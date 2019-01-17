/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.audit.AuditTrailDecoderHandle;
import com.elster.jupiter.audit.AuditTrailReference;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.TableSpecs;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.energyict.mdc.device.data.audit.AuditTrailDeviceProtocol",
        service = {AuditTrailDecoderHandle.class},
        immediate = true)
public class AuditTrailDeviceProtocol implements AuditTrailDecoderHandle {

    public static final String TABLE_IDENTIFIER = TableSpecs.DDC_DEVICEPROTOCOLPROPERTY.name();
    private volatile OrmService ormService;
    private volatile PropertyValueInfoService propertyValueInfoService;
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

    @Override
    public String getDomain() {
        return AuditDomainType.DEVICE.name();
    }

    @Override
    public String getContext() {
        return AuditDomainContextType.GENERAL_ATTRIBUTES.name();
    }

    @Override
    public List<String> getPrivileges() {
        return Arrays.asList("privilege.view.device", "privilege.administrate.device", "privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication");
    }

    @Override
    public AuditDecoder getAuditDecoder(AuditTrailReference reference) {
        return new AuditTrailDeviceProtocolDecoder(ormService, deviceService, propertyValueInfoService).init(reference);
    }
}
