/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.generalAttributes;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditTrailDecoderHandle;
import com.elster.jupiter.audit.AuditTrailReference;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.metering.impl.audit.general.attributes",
        service = {AuditTrailDecoderHandle.class},
        immediate = true)
public class AuditTrailGeneralAttributesHandle implements AuditTrailDecoderHandle {

    private final AuditDomainContextType auditDomainContextType = AuditDomainContextType.USAGEPOINT_GENERAL_ATTRIBUTES;

    private volatile OrmService ormService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    @SuppressWarnings("unused") // for OSGI
    public AuditTrailGeneralAttributesHandle() {
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN).join(nlsService.getThesaurus("UPL", Layer.DOMAIN));
    }

    @Override
    public AuditDomainContextType getAuditDomainContextType() {
        return auditDomainContextType;
    }

    @Override
    public List<String> getPrivileges() {
        return Arrays.asList(Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT);
    }

    @Override
    public AuditDecoder getAuditDecoder(AuditTrailReference reference) {
        return new AuditTrailGeneralAttributesDecoder(ormService, thesaurus, meteringService).init(reference);
    }
}
