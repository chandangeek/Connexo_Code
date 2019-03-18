/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.audit.rest.AuditInfo;
import com.elster.jupiter.audit.rest.AuditInfoFactory;
import com.elster.jupiter.nls.Thesaurus;

import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.audit.rest.impl.AuditInfoFactoryImpl",
        service = {AuditInfoFactory.class},
        immediate = true)
public class AuditInfoFactoryImpl implements AuditInfoFactory {

    @Inject
    public AuditInfoFactoryImpl() {
    }

    @Override
    public AuditInfo from(AuditTrail audit, Thesaurus thesaurus) {
        AuditInfo auditInfo = new AuditInfo();
        AuditDomainContextType auditDomainContextType =
                (audit.getDomainContext() == AuditDomainContextType.DEVICE_ATTRIBUTES) && (audit.getOperation() == AuditOperationType.DELETE) ?
                        AuditDomainContextType.NODOMAIN : audit.getDomainContext();
        auditInfo.id = audit.getId();
        auditInfo.domain = thesaurus.getString(audit.getDomainContext().domainType().name(), audit.getDomainContext().domainType().name());
        auditInfo.context = thesaurus.getString(auditDomainContextType.type(), getDefultTranslation(auditDomainContextType));
        auditInfo.contextType = auditDomainContextType;
        auditInfo.domainType = audit.getDomainContext().domainType();
        auditInfo.changedOn = audit.getChangedOn();
        auditInfo.operation = thesaurus.getString(audit.getOperation().name(), audit.getOperation().name());
        auditInfo.operationType = audit.getOperation().name();
        auditInfo.user = audit.getUser();
        auditInfo.auditReference = new AuditReferenceFactory().from(audit.getTouchDomain());
        auditInfo.auditLogs = audit.getLogs()
                .stream()
                .map(auditLogChanges -> new AuditLogInfoFactory().from(auditLogChanges))
                .sorted((auditLog1, auditLog2) -> auditLog1.getName().compareToIgnoreCase(auditLog2.getName()))
                .collect(Collectors.toList());
        return auditInfo;
    }

    private String getDefultTranslation(AuditDomainContextType auditDomainContextType) {
        return Arrays.stream(AuditDomainContextTranslationKeys.values())
                .filter(auditDomainContextTranslationKeys -> auditDomainContextTranslationKeys.getKey().compareToIgnoreCase(auditDomainContextType.name()) == 0)
                .findFirst()
                .map(AuditDomainContextTranslationKeys::getDefaultFormat)
                .orElseGet(() -> AuditDomainContextTranslationKeys.NODOMAIN.getDefaultFormat());
    }
}