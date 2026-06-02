// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import java.util.Map;

public class TestPrimitivesConfig {
    // one field for each primitive type
    public int intField = 0;
    public long longField = 0L;
    public double doubleField = 0.0;
    public float floatField = 0.0f;
    public boolean booleanField = false;
    public short shortField = 0;
    public byte byteField = 0;
    public char charField = '\0';

    // a map of <String,Integer>
    public Map<String, Integer> mapStringInt = null;

    // nested subclass with an int member and a map of <String,String>
    public SubConfig nested = new SubConfig();

    public static class SubConfig {
        public int subInt = 0;
        public Map<String, String> subMap = null;
    }
}
