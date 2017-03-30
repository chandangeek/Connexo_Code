/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.status;

import com.energyict.mdc.engine.config.ComServer;

/**
 * Models the different types of {@link com.energyict.mdc.engine.config.ComServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (11:05)
 */
public enum ComServerType {

    ONLINE {
        @Override
        protected boolean appliesTo(ComServer comServer) {
            return comServer.isOnline();
        }
    },

    REMOTE {
        @Override
        protected boolean appliesTo(ComServer comServer) {
            return comServer.isRemote();
        }
    },

    MOBILE {
        @Override
        protected boolean appliesTo(ComServer comServer) {
            return comServer.isOffline();
        }
    },

    NOT_APPLICABLE {
        @Override
        protected boolean appliesTo(ComServer comServer) {
            return false;
        }
    };

    public static ComServerType typeFor(ComServer comServer) {
        for (ComServerType type : values()) {
            if (type.appliesTo(comServer)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Type of comserver (" + comServer.getName() + ") is not supported");
    }

    protected abstract boolean appliesTo(ComServer comServer);

}