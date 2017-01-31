/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

public enum ComPortType {

    SERIAL {
        @Override
        public String toString () {
            return "TYPE_SERIAL";
        }
    },

    TCP {
        @Override
        public String toString () {
            return "TYPE_TCP";
        }
    },

    UDP {
        @Override
        public String toString () {
            return "TYPE_UDP";
        }
    },

    SERVLET {
        @Override
        public String toString () {
            return "TYPE_SERVLET";
        }
    };

    public int dbValue() {
        return this.ordinal();
    }

}