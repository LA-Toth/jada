// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.core;

public enum Direction {
    IN {
        @Override
        public boolean isIn() {
            return true;
        }

        public String toString() {
            return "inbound";
        }
    },
    OUT {
        @Override
        public boolean isOut() {
            return true;
        }

        public String toString() {
            return "outbound";
        }
    };

    public static Direction otherDirection(Direction direction) {
        return direction.isIn() ? Direction.OUT : Direction.IN;
    }

    public boolean isIn() {
        return false;
    }

    public boolean isInbound() {
        return isIn();
    }

    public boolean isOut() {
        return false;
    }

    public boolean isOutbound() {
        return isOut();
    }

    public Direction otherDirection() {
        return otherDirection(this);
    }
}
