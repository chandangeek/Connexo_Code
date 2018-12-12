/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.adapters;


import com.energyict.mdc.upl.nls.MessageSeed;

import java.util.logging.Level;

/**
 * Adapter between UPL and Connexo MessageSeed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (09:43)
 */
public class ConnexoMessageSeedAdapter implements com.elster.jupiter.util.exception.MessageSeed {

    private final MessageSeed actual;

    public static com.elster.jupiter.util.exception.MessageSeed adaptTo(MessageSeed actual) {
        return new ConnexoMessageSeedAdapter(actual);
    }

    private ConnexoMessageSeedAdapter(MessageSeed actual) {
        this.actual = actual;
    }

    public MessageSeed getUplMessageSeed() {
        return actual;
    }

    @Override
    public String getKey() {
        return this.actual.getKey();
    }

    @Override
    public String getDefaultFormat() {
        return this.actual.getDefaultFormat();
    }

    @Override
    public String getModule() {
        return this.actual.getModule();
    }

    @Override
    public int getNumber() {
        return this.actual.getNumber();
    }

    @Override
    public Level getLevel() {
        return this.actual.getLevel();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnexoMessageSeedAdapter) {
            return actual.equals(((ConnexoMessageSeedAdapter) obj).actual);
        } else {
            return actual.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }
}