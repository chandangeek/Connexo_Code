package com.energyict.mdc.ports;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "comPortType")
@XmlEnum
public enum ComPortType {

    SERIAL {
        @Override
        public String toString() {
            return "TYPE_SERIAL";
        }
    },

    TCP {
        @Override
        public String toString() {
            return "TYPE_TCP";
        }
    },

    UDP {
        @Override
        public String toString() {
            return "TYPE_UDP";
        }
    },

    SERVLET {
        @Override
        public String toString() {
            return "TYPE_SERVLET";
        }
    },

    EXTERNAL_SERVLET {
        @Override
        public String toString() {
            return "TYPE_EXTERNAL_SERVLET";
        }
    },

    COAP {
        @Override
        public String toString() {
            return "TYPE_COAP";
        }
    };

    public int dbValue() {
        return this.ordinal();
    }

    public static ComPortType valueFromDb(int dbValue) {
        for (ComPortType comPortType : values()) {
            if (comPortType.dbValue() == dbValue) {
                return comPortType;
            }
        }
        throw new IllegalArgumentException("unknown dbValue: " + dbValue);
    }

}