/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChanges;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuditEndDeviceDecoder implements AuditDecoder {

    private volatile MeteringService meteringService;
    private String reference;
    private Optional<Meter> endDevice = Optional.empty();

    AuditEndDeviceDecoder(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public AuditEndDeviceDecoder init(String reference) {
        this.reference = reference;
        decodeReference();
        return this;
    }

    @Override
    public String getName() {
        return endDevice
                .map(Meter::getName)
                .orElseThrow(() -> new IllegalArgumentException("End device cannot be found"));
    }

    @Override
    public Object getReference() {
        return endDevice
                .map(Meter::getName)
                .orElseThrow(() -> new IllegalArgumentException("End device cannot be found"));
    }

    @Override
    public List<AuditLogChanges> getAuditLogChanges() {
        return Collections.emptyList();
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        if ((context == AuditDomainContextType.GENERAL_ATTRIBUTES) && (operation.equals(UnexpectedNumberOfUpdatesException.Operation.INSERT))) {
            return UnexpectedNumberOfUpdatesException.Operation.UPDATE;
        }
        return operation;
    }

    private void decodeReference() {
        try {
            JSONObject jsonData = new JSONObject(reference);
            Long endDeviceId = ((Number) jsonData.get("ID")).longValue();
            endDevice = meteringService.findMeterById(endDeviceId);
        } catch (JSONException e) {
        }
    }

}
