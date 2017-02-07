/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelType;

public class VoidTestComChannel implements ComChannel {

    private TypedProperties connectionTaskProperties = TypedProperties.empty();

    @Override
    public int available() {
        return 0;
    }

    @Override
    public final void close () {
        // nothing to close
    }

    @Override
    public final void flush () {
        // nothing to do
    }

    @Override
    public final boolean startReading () {
        //nothing to do
        return true;
    }

    @Override
    public final int read () {
        return 0;
    }

    @Override
    public final int read (byte[] buffer) {
        return 0;
    }

    @Override
    public final int read (byte[] buffer, int offset, int length) {
        return 0;
    }

    @Override
    public final boolean startWriting () {
        //nothing to do
        return true;
    }

    @Override
    public final int write (int b) {
        return 0;
    }

    @Override
    public final int write (byte[] bytes) {
        return 0;
    }

    @Override
    public TypedProperties getProperties() {
        return this.connectionTaskProperties;
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.NOT_DEFINED;
    }

    @Override
    public void addProperties(TypedProperties typedProperties) {
        this.connectionTaskProperties.setAllProperties(typedProperties);
    }

}