/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

/**
 * Models the modem types that are supported by the mdc engine.
 * Use the provided constants in your &#64;Reference annotations
 * to pick the correct {@link SerialComponentService}.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-11-03 (10:09)
*/
public enum ModemType {
    None {
        @Override
        public String atReferenceTarget() {
            return Target.NONE;
        }
    },

    At {
        @Override
        public String atReferenceTarget() {
            return Target.AT;
        }
    },

    PakNet {
        @Override
        public String atReferenceTarget() {
            return Target.PAKNET;
        }
    },

    PEMP {
        @Override
        public String atReferenceTarget() {
            return Target.PEMP;
        }
    },

    Case {
        @Override
        public String atReferenceTarget() {
            return Target.CASE;
        }
    };

    public abstract String atReferenceTarget();

    public static class Target {
        public static final String NONE = "none";
        public static final String AT = "at";
        public static final String PAKNET = "paknet";
        public static final String PEMP = "pemp";
        public static final String CASE = "case";
    }

}