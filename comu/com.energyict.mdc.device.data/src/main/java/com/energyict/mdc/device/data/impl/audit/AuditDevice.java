/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDecoderHandle;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.TableSpecs;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
        name = "com.energyict.mdc.device.data.audit.AuditDevice",
        service = {AuditDecoderHandle.class},
        immediate = true)
public class AuditDevice implements AuditDecoderHandle {

    public static final String TABLE_IDENTIFIER = TableSpecs.DDC_DEVICE.name();

    private volatile DeviceService deviceService;

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public String getIdentifier() {
        return TABLE_IDENTIFIER;
    }

    @Override
    public AuditDecoder getAuditDecoder(String reference) {
        return new AuditDeviceDecoder(deviceService).init(reference);
    }
}
