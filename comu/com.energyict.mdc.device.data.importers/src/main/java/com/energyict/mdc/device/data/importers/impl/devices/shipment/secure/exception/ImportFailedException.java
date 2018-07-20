/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception;

import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

public class ImportFailedException extends RuntimeException {
    private final MessageSeeds messageSeeds;
    private final Object[] objects;

    public ImportFailedException(MessageSeeds messageSeeds, Object... objects) {
        this.messageSeeds = messageSeeds;
        this.objects = objects;
    }

    public MessageSeeds getMessageSeed() {
        return messageSeeds;
    }

    public Object[] getMessageParameters() {
        return objects;
    }
}