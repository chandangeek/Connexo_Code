/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.connectionMethods;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditTrailDecoderHandle;
import com.elster.jupiter.audit.AuditTrailReference;
import com.elster.jupiter.cps.CustomPropertySetService;
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
        name = "com.energyict.mdc.device.data.audit.connectionMethod",
        service = {AuditTrailDecoderHandle.class},
        immediate = true)
public class AuditTrailConnectionMethodHandle implements AuditTrailDecoderHandle {

    private final AuditDomainContextType auditDomainContextType = AuditDomainContextType.DEVICE_CONNECTION_METHODS;

    private volatile OrmService ormService;
    private volatile ServerDeviceService serverDeviceService;
    private volatile MeteringService meteringService;
    private volatile CustomPropertySetService customPropertySetService;
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
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public AuditDomainContextType getAuditDomainContextType() {
        return auditDomainContextType;
    }

    @Override
    public List<String> getPrivileges() {
        return Arrays.asList(
                "privilege.view.device",
                "privilege.administrate.device",
                "privilege.administrate.deviceCommunication",
                "privilege.operate.deviceCommunication");
    }

    @Override
    public AuditDecoder getAuditDecoder(AuditTrailReference reference) {
        return new AuditTrailConnectionMethodDecoder(ormService, thesaurus, meteringService, serverDeviceService, customPropertySetService).init(reference);
    }
}
