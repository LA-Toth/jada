// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionConfigLoaderTest {

    @Test
    public void testPopulatePrimitivesAndMaps() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("intField", 42);
        map.put("longField", 1234567890123L);
        map.put("doubleField", 3.1415);
        map.put("floatField", 2.5f);
        map.put("booleanField", true);
        map.put("shortField", (short) 7);
        map.put("byteField", (byte) 8);
        map.put("charField", "Z"); // character can be provided as a one-char string

        Map<String, Object> mi = new HashMap<>();
        mi.put("one", 1);
        mi.put("two", 2);
        map.put("mapStringInt", mi);

        Map<String, Object> nested = new HashMap<>();
        nested.put("subInt", 99);
        Map<String, Object> sm = new HashMap<>();
        sm.put("k", "v");
        nested.put("subMap", sm);
        map.put("nested", nested);

        TestPrimitivesConfig cfg = ReflectionConfigLoader.createFromMap(TestPrimitivesConfig.class, map);

        assertEquals(42, cfg.intField);
        assertEquals(1234567890123L, cfg.longField);
        assertEquals(3.1415, cfg.doubleField, 0.000001);
        assertEquals(2.5f, cfg.floatField, 0.00001f);
        assertTrue(cfg.booleanField);
        assertEquals((short)7, cfg.shortField);
        assertEquals((byte)8, cfg.byteField);
        assertEquals('Z', cfg.charField);

        assertNotNull(cfg.mapStringInt);
        assertEquals(2, cfg.mapStringInt.get("two").intValue());

        assertNotNull(cfg.nested);
        assertEquals(99, cfg.nested.subInt);
        assertNotNull(cfg.nested.subMap);
        assertEquals("v", cfg.nested.subMap.get("k"));
    }
}
