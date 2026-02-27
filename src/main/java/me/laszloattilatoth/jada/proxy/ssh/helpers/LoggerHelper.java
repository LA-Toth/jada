// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.helpers;

import me.laszloattilatoth.jada.proxy.ssh.core.Constant;

public class LoggerHelper {
    public static final String NOT_DEFINED = "(not predefined)";

    public static String formatSideStr(boolean client_to_server) {
        return client_to_server ? "client->server" : "server->client";
    }

    public static String packetTypeName(int packetType) {
        return Constant.SSH_MSG_NAMES[packetType] != null ? Constant.SSH_MSG_NAMES[packetType] : NOT_DEFINED;
    }

    private LoggerHelper() {
        throw new UnsupportedOperationException("No instance allowed");
    }
}
