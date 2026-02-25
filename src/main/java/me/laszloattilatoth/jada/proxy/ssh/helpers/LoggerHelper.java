/*
 * Copyright 2026 Laszlo Attila Toth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
