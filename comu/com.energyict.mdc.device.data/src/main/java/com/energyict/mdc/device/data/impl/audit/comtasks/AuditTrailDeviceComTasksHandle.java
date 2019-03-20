package com.energyict.mdc.device.data.impl.audit.comtasks;


import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditTrailDecoderHandle;
import com.elster.jupiter.audit.AuditTrailReference;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;

import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.deviceAttributes.AuditTrailDeviceAtributesDecoder;
import com.energyict.mdc.tasks.TaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.energyict.mdc.device.data.audit.comtasks",
        service = {AuditTrailDecoderHandle.class},
        immediate = true)
public class AuditTrailDeviceComTasksHandle implements AuditTrailDecoderHandle {
    private final AuditDomainContextType auditDomainContextType = AuditDomainContextType.DEVICE_COMTASKS;

    private volatile OrmService ormService;
    private volatile ServerDeviceService serverDeviceService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    @SuppressWarnings("unused") // for OSGI
    public AuditTrailDeviceComTasksHandle() {
    }

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
    public AuditDomainContextType getAuditDomainContextType() {
        return auditDomainContextType;
    }

    @Override
    public List<String> getPrivileges() {
        return Arrays.asList("privilege.view.device", "privilege.administrate.deviceData");
    }

    @Override
    public AuditDecoder getAuditDecoder(AuditTrailReference reference) {
        return new AuditTrailDeviceComTasksDecoder(ormService, thesaurus, meteringService, serverDeviceService).init(reference);
    }
}

