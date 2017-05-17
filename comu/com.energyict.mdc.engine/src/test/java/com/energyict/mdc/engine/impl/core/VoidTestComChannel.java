/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;

/**
 * Provides an implementation of the {@link ComChannel} interface
 * that can be handled by the ComServer framework, but that basically does nothing.
 * It will mostly be created when exceptions occur during the creation of Inbound connections.
 * <p>
 *
 * Date: 18/10/12
 * Time: 14:06
 */
public class VoidTestComChannel implements ComChannel {

    private TypedProperties connectionTaskProperties = TypedProperties.empty();

    @Override
    public int available() {
        return 0;
    }

    @Override
    public final void close() {
        // nothing to close
    }

    @Override
    public final void flush() {
        // nothing to do
    }

    @Override
    public final boolean startReading() {
        //nothing to do
        return true;
    }

    @Override
    public final int read() {
        return 0;
    }

    @Override
    public final int read(byte[] buffer) {
        return 0;
    }

    @Override
    public final int read(byte[] buffer, int offset, int length) {
        return 0;
    }

    @Override
    public final boolean startWriting() {
        //nothing to do
        return true;
    }

    @Override
    public final int write(int b) {
        return 0;
    }

    @Override
    public final int write(byte[] bytes) {
        return 0;
    }

    @Override
    public TypedProperties getProperties() {
        return this.connectionTaskProperties;
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.Invalid;
    }

    @Override
    public void addProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        this.connectionTaskProperties.setAllProperties(typedProperties);
    }

    @Override
    public void prepareForDisConnect() {
        //Do nothing
    }

    @Override
    public void setTimeout(long millis) {
        //Do nothing
    }

    @Override
    public boolean isVoid() {
        return true;
    }
}