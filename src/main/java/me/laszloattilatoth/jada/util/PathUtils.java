// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.util;

public class PathUtils {

    public static String expandUser(String path) {
        if (path.startsWith("~"))
            return path.replaceFirst("~", System.getProperty("user.home"));
        // FIXME: ~user, eg. ~root is not supported by now
        return path;
    }
}
