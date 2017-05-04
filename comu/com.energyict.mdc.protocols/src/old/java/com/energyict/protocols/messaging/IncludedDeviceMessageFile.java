/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.DeviceMessageFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Implements {@link DeviceMessageFile} to provide for an included user file as opposed to a user file that has been fetched
 * from the database. This is useful when we embed the file in the message instead of just sending the ID, as is done
 * when sending upgrades through the RTU+Server for instance (because we don't have access to the Oracle database there).
 *
 * @author alex
 */
final class IncludedDeviceMessageFile implements DeviceMessageFile {

    /**
     * The file contents.
     */
    private final String contents;

    /**
     * Creates a new included file specifying the contents.
     *
     * @param contents The contents of the file. Note that as this is a String, if you want to send a binary file
     *                 it has to be encoded first.
     */
    IncludedDeviceMessageFile(final String contents) {
        this.contents = contents;
    }

    @Override
    public void readWith(Consumer<InputStream> inputStreamConsumer) {
        inputStreamConsumer.accept(new BufferedInputStream(new ByteArrayInputStream(this.contents.getBytes())));
    }

    @Override
    public final String getName() {
        return null;
    }

    @Override
    public final long getId() {
        return 0;
    }

}