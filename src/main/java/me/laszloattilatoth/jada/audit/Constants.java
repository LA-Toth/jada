/*
 * Copyright 2021 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.audit;

public class Constants {
    public static final byte[] FILE_MAGIC = new byte[]{'P', 'J', 'A', 'T'};
    public static final int FILE_VERSION = 1;

    /* FILE HEADER FLAGS */
    public static final int FF_IN_DEV = 1; /// The current file version structure is in development
    public static final int FF_RESERVED1 = 2; /// Placeholder
    public static final int FF_RESERVED2 = 4; /// Placeholder
    public static final int FF_ADDITIONAL_FLAGS = 8; /// Placeholder if the remaining 12 bits are not enough. TBD.

    /* packet types */
    public static final int PKT_PROTOCOL_DETAILS = 1;  /// Additional details not fitting into the file header
    public static final int PKT_CLIENT_TO_SERVER = 2;
    public static final int PKT_SERVER_TO_CLIENT = 3;
    public static final int PKT_INTEGRITY_CHECK = 4;
    public static final int PKT_CHANNEL_DETAILS = 5;  /// (SSH) channel-specific details when a channel is opened or rejected
    public static final int PKT_METADATA = 6;  /// Similar to proto and channel details but non-specific to either of those.
}
