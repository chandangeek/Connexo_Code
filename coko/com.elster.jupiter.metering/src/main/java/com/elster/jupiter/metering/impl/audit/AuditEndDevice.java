/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDecoderHandle;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.TableSpecs;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;

@Component(
        name = "com.elster.jupiter.metering.impl.audit.AuditEndDevice",
        service = {AuditDecoderHandle.class},
        immediate = true)
public class AuditEndDevice implements AuditDecoderHandle {

    public static final String TABLE_IDENTIFIER = TableSpecs.MTR_ENDDEVICE.name();

    private volatile MeteringService meteringService;

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public String getDomain() {
        return TABLE_IDENTIFIER;
    }

    @Override
    public List<String> getPrivileges() {
        return Collections.emptyList();
    }

    @Override
    public AuditDecoder getAuditDecoder(String reference) {
        return new AuditEndDeviceDecoder(meteringService).init(reference);
    }
}
