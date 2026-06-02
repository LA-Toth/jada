// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.core;

/**
 * Represents which side of the proxy connection is being referenced.
 * <p>
 * From the proxy's perspective:
 * <ul>
 * <li>{@link #CLIENT} - The client that initiates the original connection to the proxy</li>
 * <li>{@link #SERVER} - The server to which the proxy connects on behalf of the client</li>
 * </ul>
 */
public enum Side {
    /**
     * The client side - represents a client that initiates a connection to the proxy.
     */
    CLIENT {
        @Override
        public boolean isClient() {
            return true;
        }

        public String toString() {
            return "client";
        }
    },
    /**
     * The server side - represents the server to which the proxy connects.
     */
    SERVER {
        @Override
        public boolean isServer() {
            return true;
        }

        public String toString() {
            return "server";
        }
    };

    /**
     * Returns the opposite side.
     *
     * @param side the side to get the opposite of
     * @return {@link #SERVER} if the given side is {@link #CLIENT}, otherwise {@link #CLIENT}
     */
    public static Side otherSide(Side side) {
        return side.isClient() ? Side.SERVER : Side.CLIENT;
    }

    /**
     * Checks whether this side represents the client.
     *
     * @return true if this is the {@link #CLIENT} side, false otherwise
     */
    public boolean isClient() {
        return false;
    }

    /**
     * Checks whether this side represents the server.
     *
     * @return true if this is the {@link #SERVER} side, false otherwise
     */
    public boolean isServer() {
        return false;
    }

    /**
     * Returns the opposite side.
     *
     * @return the opposite {@link Side}
     */
    public Side otherSide() {
        return otherSide(this);
    }
}
