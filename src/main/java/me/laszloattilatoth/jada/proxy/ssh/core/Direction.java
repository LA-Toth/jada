// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.core;

/**
 * Represents the direction of an SSH traffic stream relative to the proxy.
 * <p>
 * {@link #IN} refers to the received (inbound) TCP stream,
 * {@link #OUT} refers to the sent (outbound) TCP stream.
 * Which physical peer (client or server) produces or consumes each stream
 * depends on the {@code Side} — see {@link Side}.
 * </p>
 */
public enum Direction {
    /** Inbound direction: the received TCP stream. */
    IN {
        @Override
        public boolean isIn() {
            return true;
        }

        @Override
        public String toString() {
            return "inbound";
        }
    },
    /** Outbound direction: the sent TCP stream. */
    OUT {
        @Override
        public boolean isOut() {
            return true;
        }

        @Override
        public String toString() {
            return "outbound";
        }
    };

    /**
     * Returns the opposite direction of the given {@code direction}.
     *
     * @param direction the direction whose opposite is returned
     * @return {@link Direction#OUT} if {@code direction} is {@link Direction#IN}, otherwise {@link Direction#IN}
     */
    public static Direction otherDirection(Direction direction) {
        return direction.isIn() ? Direction.OUT : Direction.IN;
    }

    /**
     * Returns {@code true} if this direction is inbound (received stream).
     *
     * @return {@code true} for {@link #IN}, {@code false} otherwise
     */
    public boolean isIn() {
        return false;
    }

    /**
     * Returns {@code true} if this direction is inbound (received stream).
     * <p>Alias for {@link #isIn()}.</p>
     *
     * @return {@code true} for {@link #IN}, {@code false} otherwise
     */
    public boolean isInbound() {
        return isIn();
    }

    /**
     * Returns {@code true} if this direction is outbound (sent stream).
     *
     * @return {@code true} for {@link #OUT}, {@code false} otherwise
     */
    public boolean isOut() {
        return false;
    }

    /**
     * Returns {@code true} if this direction is outbound (sent stream).
     * <p>Alias for {@link #isOut()}.</p>
     *
     * @return {@code true} for {@link #OUT}, {@code false} otherwise
     */
    public boolean isOutbound() {
        return isOut();
    }

    /**
     * Returns the opposite direction of this instance.
     * <p>Convenience wrapper for {@link #otherDirection(Direction)}.</p>
     *
     * @return {@link Direction#OUT} if this is {@link Direction#IN}, otherwise {@link Direction#IN}
     */
    public Direction otherDirection() {
        return otherDirection(this);
    }
}
