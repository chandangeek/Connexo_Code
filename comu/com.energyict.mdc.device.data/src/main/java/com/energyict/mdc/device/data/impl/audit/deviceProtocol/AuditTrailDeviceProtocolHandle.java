/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.deviceProtocol;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditTrailDecoderHandle;
import com.elster.jupiter.audit.AuditTrailReference;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.energyict.mdc.device.data.audit.deviceProtocol",
        service = {AuditTrailDecoderHandle.class},
        immediate = true)
public class AuditTrailDeviceProtocolHandle implements AuditTrailDecoderHandle {

    private final AuditDomainContextType auditDomainContextType = AuditDomainContextType.GENERAL_ATTRIBUTES;

    private volatile OrmService ormService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public AuditDomainContextType getAuditDomainContextType() {
        return auditDomainContextType;
    }

    @Override
    public List<String> getPrivileges() {
        return Arrays.asList("privilege.view.device", "privilege.administrate.device", "privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication");
    }

    @Override
    public AuditDecoder getAuditDecoder(AuditTrailReference reference) {
        return new AuditTrailDeviceProtocolDecoder(ormService, meteringService, deviceService, propertyValueInfoService).init(reference);
    }
}
