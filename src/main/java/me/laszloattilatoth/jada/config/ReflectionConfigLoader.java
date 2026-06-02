// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

/**
 * Reflection based configuration loader and simple validation support.
 * <p>
 * Usage example (in a class that will be loaded from a Map):
 * <pre>{@code
 * public class MyOptions {
 *   &#64;ReflectionConfigLoader.ConfigKey("port")
 *   &#64;ReflectionConfigLoader.Range(min = 1, max = 65535)
 *   public int port = 22;
 *
 *   // Nested classes are supported and populated recursively.
 *   public SideOptions client = new SideOptions();
 * }
 *
 * MyOptions opts = ReflectionConfigLoader.createFromMap(MyOptions.class, map);
 * }</pre>
 * Validation:
 * - @Range(min,max) is implemented and applied to numeric fields (int/long/float/double)
 * - You can add further validation annotations by defining a new annotation and handling it in
 *   ReflectionConfigLoader.applyValidations
 */
public final class ReflectionConfigLoader {

    private ReflectionConfigLoader() {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    /**
     * Overrides the map key used for a field.
     */
    public @interface ConfigKey {
        /**
         * Configuration key name expected in the source map.
         *
         * @return key name
         */
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    /**
     * Numeric range validation annotation for scalar fields.
     */
    public @interface Range {
        /** @return inclusive lower bound */
        double min() default Double.NEGATIVE_INFINITY;
        /** @return inclusive upper bound */
        double max() default Double.POSITIVE_INFINITY;
    }

    /**
     * Create a new instance of clazz and populate it from map.
     *
     * @param clazz target type with no-arg constructor
     * @param map source map
     * @param <T> target type
     * @return populated instance
     * @throws ProxyOptions.InvalidOptions when instantiation or mapping fails
     */
    public static <T> T createFromMap(Class<T> clazz, Map<String, Object> map) throws ProxyOptions.InvalidOptions {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            if (!ctor.canAccess(null)) ctor.setAccessible(true);
            T instance = ctor.newInstance();
            populate(instance, map);
            return instance;
        } catch (ProxyOptions.InvalidOptions e) {
            throw e;
        } catch (Exception e) {
            throw new ProxyOptions.InvalidOptions("Failed to instantiate " + clazz.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Populate an existing instance's public/protected/private fields from the provided map.
     * Supports String, primitive numbers, their boxed types, boolean, nested classes and Map fields.
     *
     * @param instance target object to populate
     * @param map source key-value map
     * @throws ProxyOptions.InvalidOptions when type conversion or validation fails
     */
    public static void populate(Object instance, Map<String, Object> map) throws ProxyOptions.InvalidOptions {
        if (map == null) return;
        Class<?> cls = instance.getClass();
        for (Field field : cls.getDeclaredFields()) {
            String keyName = resolveKeyName(field);
            Object rawValue = findValueForKey(map, keyName, field.getName());
            if (rawValue == null) continue; // nothing to set

            boolean accessible = field.canAccess(instance);
            if (!accessible) field.setAccessible(true);
            try {
                setFieldValue(instance, field, rawValue);
                applyValidations(instance, field);
            } catch (IllegalAccessException e) {
                throw new ProxyOptions.InvalidOptions("Cannot set field " + field.getName() + ": " + e.getMessage());
            } finally {
                if (!accessible) field.setAccessible(false);
            }
        }
    }

    private static String resolveKeyName(Field field) {
        ConfigKey a = field.getAnnotation(ConfigKey.class);
        if (a != null && a.value() != null && !a.value().isEmpty()) return a.value();
        return field.getName();
    }

    private static Object findValueForKey(Map<String, Object> map, String keyName, String fieldName) {
        // try exact field name
        if (map.containsKey(keyName)) return map.get(keyName);

        // try kebab-case (camelCase -> kebab-case)
        String kebab = toKebabCase(fieldName);
        if (map.containsKey(kebab)) return map.get(kebab);

        // also try underscores
        String underscored = fieldName.replaceAll("([A-Z])", "_$1").toLowerCase(Locale.ROOT);
        if (map.containsKey(underscored)) return map.get(underscored);

        // special fallback: replace "algorithms" suffix with "algos" and check kebab form
        if (fieldName.toLowerCase(Locale.ROOT).endsWith("algorithms")) {
            String alt = fieldName.substring(0, fieldName.length() - "algorithms".length()) + "algos";
            if (map.containsKey(alt)) return map.get(alt);
            String altKebab = toKebabCase(alt);
            if (map.containsKey(altKebab)) return map.get(altKebab);
        }

        return null;
    }

    private static String toKebabCase(String camel) {
        StringBuilder sb = new StringBuilder();
        for (char c : camel.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('-').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert and assign a source value to a field.
     */
    private static void setFieldValue(Object instance, Field field, Object rawValue) throws IllegalAccessException, ProxyOptions.InvalidOptions {
        Class<?> t = field.getType();
        if (rawValue == null) {
            field.set(instance, null);
            return;
        }

        if (t == String.class) {
            field.set(instance, String.valueOf(rawValue));
            return;
        }

        if (t == int.class || t == Integer.class) {
            Number n = toNumber(rawValue);
            field.set(instance, n.intValue());
            return;
        }
        if (t == short.class || t == Short.class) {
            Number n = toNumber(rawValue);
            field.set(instance, n.shortValue());
            return;
        }
        if (t == byte.class || t == Byte.class) {
            Number n = toNumber(rawValue);
            field.set(instance, n.byteValue());
            return;
        }
        if (t == long.class || t == Long.class) {
            Number n = toNumber(rawValue);
            field.set(instance, n.longValue());
            return;
        }
        if (t == double.class || t == Double.class) {
            Number n = toNumber(rawValue);
            field.set(instance, n.doubleValue());
            return;
        }
        if (t == float.class || t == Float.class) {
            Number n = toNumber(rawValue);
            field.set(instance, n.floatValue());
            return;
        }
        if (t == boolean.class || t == Boolean.class) {
            if (rawValue instanceof Boolean) field.set(instance, rawValue);
            else field.set(instance, Boolean.parseBoolean(String.valueOf(rawValue)));
            return;
        }

        if (t == char.class || t == Character.class) {
            if (rawValue instanceof Character) {
                field.set(instance, rawValue);
                return;
            } else if (rawValue instanceof String) {
                String s = (String) rawValue;
                if (!s.isEmpty()) {
                    field.set(instance, s.charAt(0));
                    return;
                }
            }
            throw new ProxyOptions.InvalidOptions("Field " + field.getName() + " expects a character value");
        }

        if (Map.class.isAssignableFrom(t)) {
            if (rawValue instanceof Map) {
                field.set(instance, rawValue);
                return;
            } else {
                throw new ProxyOptions.InvalidOptions("Field " + field.getName() + " expects a map but value is " + rawValue.getClass().getSimpleName());
            }
        }

        // nested object: expect a Map and recursive population
        if (rawValue instanceof Map) {
            try {
                Object nested = field.get(instance);
                if (nested == null) {
                    Constructor<?> ctor = t.getDeclaredConstructor();
                    if (!ctor.canAccess(null)) ctor.setAccessible(true);
                    nested = ctor.newInstance();
                    field.set(instance, nested);
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) rawValue;
                populate(nested, nestedMap);
                return;
            } catch (ProxyOptions.InvalidOptions e) {
                throw e;
            } catch (Exception e) {
                throw new ProxyOptions.InvalidOptions("Failed to create nested instance for field " + field.getName() + ": " + e.getMessage());
            }
        }

        // last resort: attempt to convert simple scalar to the expected type
        if (isWrapperOrPrimitive(t) && rawValue instanceof String) {
            String s = (String) rawValue;
            if (t == Integer.class || t == int.class) field.set(instance, Integer.parseInt(s));
            else if (t == Long.class || t == long.class) field.set(instance, Long.parseLong(s));
            else if (t == Double.class || t == double.class) field.set(instance, Double.parseDouble(s));
            else if (t == Float.class || t == float.class) field.set(instance, Float.parseFloat(s));
            else if (t == Boolean.class || t == boolean.class) field.set(instance, Boolean.parseBoolean(s));
            else field.set(instance, s);
            return;
        }

        throw new ProxyOptions.InvalidOptions("Unsupported field type " + t.getName() + " for field " + field.getName());
    }

    /**
     * Convert a scalar value to a number.
     */
    private static Number toNumber(Object rawValue) throws ProxyOptions.InvalidOptions {
        if (rawValue instanceof Number) return (Number) rawValue;
        if (rawValue instanceof String) {
            String s = ((String) rawValue).trim();
            try {
                if (s.contains(".")) return Double.parseDouble(s);
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new ProxyOptions.InvalidOptions("Invalid numeric value: " + s);
            }
        }
        throw new ProxyOptions.InvalidOptions("Expected numeric value but got " + rawValue.getClass().getSimpleName());
    }

    /**
     * Check whether a type is primitive or a supported wrapper.
     */
    private static boolean isWrapperOrPrimitive(Class<?> t) {
        return t.isPrimitive() || t == Integer.class || t == Long.class || t == Double.class || t == Float.class || t == Boolean.class || t == Short.class || t == Byte.class || t == Character.class;
    }

    /**
     * Apply field-level validation annotations after assignment.
     */
    private static void applyValidations(Object instance, Field field) throws IllegalAccessException, ProxyOptions.InvalidOptions {
        Object v = field.get(instance);
        if (v == null) return;

        Range r = field.getAnnotation(Range.class);
        if (r != null) {
            double vnum;
            if (v instanceof Number) vnum = ((Number) v).doubleValue();
            else if (v instanceof String) {
                try { vnum = Double.parseDouble((String) v); } catch (NumberFormatException e) { throw new ProxyOptions.InvalidOptions("Field " + field.getName() + " is not numeric for Range validation"); }
            } else throw new ProxyOptions.InvalidOptions("Field " + field.getName() + " is not numeric for Range validation");

            if (vnum < r.min() || vnum > r.max()) {
                throw new ProxyOptions.InvalidOptions(String.format("Field %s value %s out of range [%s,%s]", field.getName(), v, r.min(), r.max()));
            }
        }

        // Placeholder: add handling for other validation annotations here
    }
}
