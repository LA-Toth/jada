// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.audit;

/*
    Topmost class for audit trail file handling.

    Every file content is defined based on the types used in SSH protocol
    https://tools.ietf.org/html/rfc4251#page-8
    Data Type Representations Used in the SSH Protocols

    Overall structure:
    byte[4] "PJAT"
    uint32  version = 1
    string file header  - First field is always uint32 flags, further: see FileHeader class.
    [string header 2 - exists if needed based on the flags. first field must be: uint32 version
     string header 3 - exists if needed based on the flags. first field must be: uint32 version
     ...]
    byte packet_type 1
    string packet_content 1 (serialized form of a record)
    [    byte packet_type 2
    string packet_content 2  (serialized form of a record)
    ...]

    Almost everything is "string" for fast indexing, to store the starting position of every header and packet.
 */
public class AuditTrailFile {
}
