/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.deviceAttributes;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.audit.AuditTrailDecoderHandle;
import com.elster.jupiter.audit.AuditTrailReference;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ServerDeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.energyict.mdc.device.data.audit.deviceAttributes",
        service = {AuditTrailDecoderHandle.class},
        immediate = true)
public class AuditTrailDeviceAttributes implements AuditTrailDecoderHandle {

    private volatile OrmService ormService;
    private volatile ServerDeviceService serverDeviceService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setServerDeviceService(ServerDeviceService serverDeviceService) {
        this.serverDeviceService = serverDeviceService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public String getDomain() {
        return AuditDomainType.DEVICE.name();
    }

    @Override
    public String getContext() {
        return AuditDomainContextType.DEVICE_ATTRIBUTES.name();
    }

    @Override
    public List<String> getPrivileges() {
        return Arrays.asList("privilege.view.device", "privilege.administrate.deviceData");
    }

    @Override
    public AuditDecoder getAuditDecoder(AuditTrailReference reference) {
        return new AuditTrailDeviceAtributesDecoder(ormService, thesaurus, meteringService, serverDeviceService).init(reference);
    }
}