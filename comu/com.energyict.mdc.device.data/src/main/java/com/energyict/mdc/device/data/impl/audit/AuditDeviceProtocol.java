/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDecoderHandle;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.TableSpecs;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
        name = "com.energyict.mdc.device.data.audit.AuditDeviceProtocol",
        service = {AuditDecoderHandle.class},
        immediate = true)
public class AuditDeviceProtocol implements AuditDecoderHandle {

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
    public String getIdentifier() {
        return TABLE_IDENTIFIER;
    }

    @Override
    public AuditDecoder getAuditDecoder(String reference) {
        return new AuditDeviceProtocolDecoder(ormService, deviceService, propertyValueInfoService).init(reference);
    }
}
