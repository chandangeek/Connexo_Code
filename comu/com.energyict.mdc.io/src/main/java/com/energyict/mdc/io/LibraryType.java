/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

/**
* Models the serial libraries that are supported by the mdc engine.
* Use the provided constants in your &#64;Reference annotations
* to pick the correct {@link SerialComponentService}.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-11-03 (10:04)
*/
public enum LibraryType {
    SeralIO {
        @Override
        public String atReferenceTarget() {
            return Target.SERIALIO;
        }
    },

    RxTx {
        @Override
        public String atReferenceTarget() {
            return Target.RXTX;
        }
    };

    public abstract String atReferenceTarget();

    public static class Target {
        public static final String SERIALIO = "serialio";
        public static final String RXTX = "rxtx";
    }

}