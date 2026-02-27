// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.core;

public enum Side {
    CLIENT {
        @Override
        public boolean isClient() { return true;}

        public String toString() {return "client"; }
    },
    SERVER {
        @Override
        public boolean isServer() { return true;}

        public String toString() {return "server";}
    };

    public static Side otherSide(Side side) {
        return side.isClient() ? Side.SERVER : Side.CLIENT;
    }

    public boolean isClient() {return false;}

    public boolean isServer() {return false;}

    public Side otherSide() {
        return otherSide(this);
    }
}
