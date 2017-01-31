/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.ComChannel;

import java.io.IOException;

/**
 * Provides an implementation for the {@link ComChannel} interface.
 * The original design provided template methods
 * for every ComChannel method so that actual subclasses
 * that reside in the protocol project could simply extend
 * this class and enjoy all the AOP advice that
 * the ComServer module had defined around ServerComChannel.
 * By that design, all of the template methods are final and have an
 * abstract doX version that needed to be implemented by subclasses.
 * <p>
 * That design is currently still in place to avoid the redo-work
 * but is not actually required anymore as the AOP advice
 * is now done on a wrapper class around ComChannel.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-06-06 (10:49)
 */
public abstract class AbstractComChannel implements ComChannel {

    private TypedProperties connectionTaskProperties = TypedProperties.empty();

    @Override
    public final void close () {
        this.doClose();
    }

    protected abstract void doClose ();

    @Override
    public final void flush () throws IOException {
        this.doFlush();
    }

    protected abstract void doFlush () throws IOException;

    @Override
    public final boolean startReading () {
        return this.doStartReading();
    }

    protected abstract boolean doStartReading ();

    @Override
    public final int read () {
        return this.doRead();
    }

    protected abstract int doRead ();

    @Override
    public final int read (byte[] buffer) {
        return this.doRead(buffer);
    }

    protected abstract int doRead (byte[] buffer);

    @Override
    public final int read (byte[] buffer, int offset, int length) {
        return this.doRead(buffer, offset, length);
    }

    protected abstract int doRead (byte[] buffer, int offset, int length);

    @Override
    public final boolean startWriting () {
        return this.doStartWriting();
    }

    protected abstract boolean doStartWriting ();

    @Override
    public final int write (int b) {
        return this.doWrite(b);
    }

    protected abstract int doWrite (int b);

    @Override
    public final int write (byte[] bytes) {
        return this.doWrite(bytes);
    }

    protected abstract int doWrite (byte[] bytes);

    @Override
    public TypedProperties getProperties() {
        return this.connectionTaskProperties;
    }

    @Override
    public void addProperties(TypedProperties typedProperties) {
        this.connectionTaskProperties.setAllProperties(typedProperties);
    }

}