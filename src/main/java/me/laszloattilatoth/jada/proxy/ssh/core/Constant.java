/*
 * Copyright 2020-2021 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.proxy.ssh.core;

public class Constant {
    /**
     * RFC 4253 6.1 Maximum packet length
     * /* The implementation MUST be able to process packets with size 35000
     */
    public static final int MINIMUM_MAX_PACKET_SIZE = 35000;

    /**
     * Maximum lines before receiving the banner line starting with SSH-...
     * <p>
     * The value is the same as in OpenSSH.
     */
    public static final int MAX_PRE_BANNER_LINES = 1024;
    public static final int MAX_BANNER_LENGTH = 8192;

    public static final String SSH_VERSION_PREFIX = "SSH-";
    public static final byte[] SSH_VERSION_PREFIX_BYTES = SSH_VERSION_PREFIX.getBytes();

    /*
        RFC 2450  SSH Protocol Assigned Numbers
        4.1.  Message Numbers
        4.1.1.  Conventions

           Protocol packets have message numbers in the range 1 to 255.  These
           numbers are allocated as follows:

              Transport layer protocol:

                1 to 19    Transport layer generic (e.g., disconnect, ignore,
                           debug, etc.)
                20 to 29   Algorithm negotiation
                30 to 49   Key exchange method specific (numbers can be reused
                           for different authentication methods)

              User authentication protocol:

                50 to 59   User authentication generic
                60 to 79   User authentication method specific (numbers can be
                           reused for different authentication methods)

              Connection protocol:

                80 to 89   Connection protocol generic
                90 to 127  Channel related messages

              Reserved for client protocols:

                128 to 191 Reserved

              Local extensions:

                192 to 255 Local extensions

     */

    // RFC 2450  SSH Protocol Assigned Numbers
    // 4.1.  Message Numbers
    // 4.1.2.  Initial Assignments
    public static final int SSH_MSG_DISCONNECT = 1;
    public static final int SSH_MSG_IGNORE = 2;
    public static final int SSH_MSG_UNIMPLEMENTED = 3;
    public static final int SSH_MSG_DEBUG = 4;
    public static final int SSH_MSG_SERVICE_REQUEST = 5;
    public static final int SSH_MSG_SERVICE_ACCEPT = 6;
    public static final int SSH_MSG_KEXINIT = 20;
    public static final int SSH_MSG_NEWKEYS = 21;
    public static final int SSH_MSG_USERAUTH_REQUEST = 50;
    public static final int SSH_MSG_USERAUTH_FAILURE = 51;
    public static final int SSH_MSG_USERAUTH_SUCCESS = 52;
    public static final int SSH_MSG_USERAUTH_BANNER = 53;
    public static final int SSH_MSG_GLOBAL_REQUEST = 80;
    public static final int SSH_MSG_REQUEST_SUCCESS = 81;
    public static final int SSH_MSG_REQUEST_FAILURE = 82;
    public static final int SSH_MSG_CHANNEL_OPEN = 90;
    public static final int SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 91;
    public static final int SSH_MSG_CHANNEL_OPEN_FAILURE = 92;
    public static final int SSH_MSG_CHANNEL_WINDOW_ADJUST = 93;
    public static final int SSH_MSG_CHANNEL_DATA = 94;
    public static final int SSH_MSG_CHANNEL_EXTENDED_DATA = 95;
    public static final int SSH_MSG_CHANNEL_EOF = 96;
    public static final int SSH_MSG_CHANNEL_CLOSE = 97;
    public static final int SSH_MSG_CHANNEL_REQUEST = 98;
    public static final int SSH_MSG_CHANNEL_SUCCESS = 99;
    public static final int SSH_MSG_CHANNEL_FAILURE = 100;

    public static final String[] SSH_MSG_NAMES = new String[256];

    // RFC 2450
    // 4.2.  Disconnection Messages Reason Codes and Descriptions
    // 4.2.2.  Initial Assignments
    // SYMBOLIC_NAME = REASON_CODE
    public static final int SSH_DISCONNECT_HOST_NOT_ALLOWED_TO_CONNECT = 1;
    public static final int SSH_DISCONNECT_PROTOCOL_ERROR = 2;
    public static final int SSH_DISCONNECT_KEY_EXCHANGE_FAILED = 3;
    public static final int SSH_DISCONNECT_RESERVED = 4;
    public static final int SSH_DISCONNECT_MAC_ERROR = 5;
    public static final int SSH_DISCONNECT_COMPRESSION_ERROR = 6;
    public static final int SSH_DISCONNECT_SERVICE_NOT_AVAILABLE = 7;
    public static final int SSH_DISCONNECT_PROTOCOL_VERSION_NOT_SUPPORTED = 8;
    public static final int SSH_DISCONNECT_HOST_KEY_NOT_VERIFIABLE = 9;
    public static final int SSH_DISCONNECT_CONNECTION_LOST = 10;
    public static final int SSH_DISCONNECT_BY_APPLICATION = 11;
    public static final int SSH_DISCONNECT_TOO_MANY_CONNECTIONS = 12;
    public static final int SSH_DISCONNECT_AUTH_CANCELLED_BY_USER = 13;
    public static final int SSH_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE = 14;
    public static final int SSH_DISCONNECT_ILLEGAL_USER_NAME = 15;
    public static final int SSH_DISCONNECT_MAX_REASON_CODE = SSH_DISCONNECT_ILLEGAL_USER_NAME;
    public static final String[] SSH_DISCONNECT_NAMES = new String[SSH_DISCONNECT_MAX_REASON_CODE + 1];

    // 4.3.  Channel Connection Failure Reason Codes and Descriptions
    // 4.3.2.  Initial Assignments - reason codes
    public static final int SSH_OPEN_ADMINISTRATIVELY_PROHIBITED = 1;
    public static final int SSH_OPEN_CONNECT_FAILED = 2;
    public static final int SSH_OPEN_UNKNOWN_CHANNEL_TYPE = 3;
    public static final int SSH_OPEN_RESOURCE_SHORTAGE = 4;

    public static final Side CLIENT_SIDE = Side.CLIENT;
    public static final Side SERVER_SIDE = Side.SERVER;

    public static final int MODE_IN = 0;
    public static final int MODE_OUT = 1;
    public static final int MODE_MAX = 2;

    static {
        SSH_MSG_NAMES[SSH_MSG_DISCONNECT] = "SSH_MSG_DISCONNECT";
        SSH_MSG_NAMES[SSH_MSG_IGNORE] = "SSH_MSG_IGNORE";
        SSH_MSG_NAMES[SSH_MSG_UNIMPLEMENTED] = "SSH_MSG_UNIMPLEMENTED";
        SSH_MSG_NAMES[SSH_MSG_DEBUG] = "SSH_MSG_DEBUG";
        SSH_MSG_NAMES[SSH_MSG_SERVICE_REQUEST] = "SSH_MSG_SERVICE_REQUEST";
        SSH_MSG_NAMES[SSH_MSG_SERVICE_ACCEPT] = "SSH_MSG_SERVICE_ACCEPT";
        SSH_MSG_NAMES[SSH_MSG_KEXINIT] = "SSH_MSG_KEXINIT";
        SSH_MSG_NAMES[SSH_MSG_NEWKEYS] = "SSH_MSG_NEWKEYS";
        SSH_MSG_NAMES[SSH_MSG_USERAUTH_REQUEST] = "SSH_MSG_USERAUTH_REQUEST";
        SSH_MSG_NAMES[SSH_MSG_USERAUTH_FAILURE] = "SSH_MSG_USERAUTH_FAILURE";
        SSH_MSG_NAMES[SSH_MSG_USERAUTH_SUCCESS] = "SSH_MSG_USERAUTH_SUCCESS";
        SSH_MSG_NAMES[SSH_MSG_USERAUTH_BANNER] = "SSH_MSG_USERAUTH_BANNER";
        SSH_MSG_NAMES[SSH_MSG_GLOBAL_REQUEST] = "SSH_MSG_GLOBAL_REQUEST";
        SSH_MSG_NAMES[SSH_MSG_REQUEST_SUCCESS] = "SSH_MSG_REQUEST_SUCCESS";
        SSH_MSG_NAMES[SSH_MSG_REQUEST_FAILURE] = "SSH_MSG_REQUEST_FAILURE";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_OPEN] = "SSH_MSG_CHANNEL_OPEN";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_OPEN_CONFIRMATION] = "SSH_MSG_CHANNEL_OPEN_CONFIRMATION";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_OPEN_FAILURE] = "SSH_MSG_CHANNEL_OPEN_FAILURE";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_WINDOW_ADJUST] = "SSH_MSG_CHANNEL_WINDOW_ADJUST";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_DATA] = "SSH_MSG_CHANNEL_DATA";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_EXTENDED_DATA] = "SSH_MSG_CHANNEL_EXTENDED_DATA";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_EOF] = "SSH_MSG_CHANNEL_EOF";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_CLOSE] = "SSH_MSG_CHANNEL_CLOSE";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_REQUEST] = "SSH_MSG_CHANNEL_REQUEST";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_SUCCESS] = "SSH_MSG_CHANNEL_SUCCESS";
        SSH_MSG_NAMES[SSH_MSG_CHANNEL_FAILURE] = "SSH_MSG_CHANNEL_FAILURE";

        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_HOST_NOT_ALLOWED_TO_CONNECT] = "SSH_DISCONNECT_HOST_NOT_ALLOWED_TO_CONNECT";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_PROTOCOL_ERROR] = "SSH_DISCONNECT_PROTOCOL_ERROR";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_KEY_EXCHANGE_FAILED] = "SSH_DISCONNECT_KEY_EXCHANGE_FAILED";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_RESERVED] = "SSH_DISCONNECT_RESERVED";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_MAC_ERROR] = "SSH_DISCONNECT_MAC_ERROR";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_COMPRESSION_ERROR] = "SSH_DISCONNECT_COMPRESSION_ERROR";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_SERVICE_NOT_AVAILABLE] = "SSH_DISCONNECT_SERVICE_NOT_AVAILABLE";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_PROTOCOL_VERSION_NOT_SUPPORTED] = "SSH_DISCONNECT_PROTOCOL_VERSION_NOT_SUPPORTED";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_HOST_KEY_NOT_VERIFIABLE] = "SSH_DISCONNECT_HOST_KEY_NOT_VERIFIABLE";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_CONNECTION_LOST] = "SSH_DISCONNECT_CONNECTION_LOST";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_BY_APPLICATION] = "SSH_DISCONNECT_BY_APPLICATION";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_TOO_MANY_CONNECTIONS] = "SSH_DISCONNECT_TOO_MANY_CONNECTIONS";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_AUTH_CANCELLED_BY_USER] = "SSH_DISCONNECT_AUTH_CANCELLED_BY_USER";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE] = "SSH_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE";
        SSH_DISCONNECT_NAMES[SSH_DISCONNECT_ILLEGAL_USER_NAME] = "SSH_DISCONNECT_ILLEGAL_USER_NAME";
    }
}
