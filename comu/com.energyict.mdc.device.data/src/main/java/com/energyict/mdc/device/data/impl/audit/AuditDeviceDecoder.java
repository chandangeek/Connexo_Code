/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditLogChanges;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;

public class AuditDeviceDecoder implements AuditDecoder {

    private volatile DeviceService deviceService;
    private String reference;

    AuditDeviceDecoder(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public AuditDeviceDecoder init(String reference) {
        this.reference = reference;
        return this;
    }

    @Override
    public String getName() {
        try {
            JSONObject jsonData = new JSONObject(reference);
            Long deviceId = ((Number) jsonData.get("DEVICEID")).longValue();
            return deviceService.findDeviceById(deviceId).map(Device::getName).orElse("");
        } catch (JSONException e) {
        }
        return "";
    }

    @Override
    public List<AuditLogChanges> getAuditLogChanges() {
        return Collections.emptyList();
    }

}
