/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.usagePoint;

import com.elster.jupiter.audit.ApplicationType;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.audit.impl.AuditServiceImpl;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.audit.generalAttributes.AuditTrailGeneralAttributesHandle;
import com.elster.jupiter.metering.impl.audit.technicalAttributes.AuditTrailTechnicalAttributesHandle;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

import com.google.common.collect.ImmutableMap;

import java.security.Principal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for the audit trail component.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditUsagePointBase {

    protected final ApplicationType applicationType = ApplicationType.MDM_APPLICATION_KEY;

    protected static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        try (TransactionContext context = getTransactionService().getContext()) {
            AuditService auditService = inMemoryBootstrapModule.getAuditService();
            ((AuditServiceImpl) auditService).addAuditTrailDecoderHandle(getGeneralAttributesHandle());
            ((AuditServiceImpl) auditService).addAuditTrailDecoderHandle(getGeneralTechnicalAttributesHandle());

            context.commit();
        }
        grantPrivilegesForCurrentUser();
    }

    static protected AuditTrailGeneralAttributesHandle getGeneralAttributesHandle(){
        AuditTrailGeneralAttributesHandle decoder = new AuditTrailGeneralAttributesHandle();
        decoder.setMeteringService(inMemoryBootstrapModule.getMeteringService());
        decoder.setNlsService(inMemoryBootstrapModule.getNlsService());
        decoder.setOrmService(inMemoryBootstrapModule.getOrmService());

        return decoder;
    }

    static protected AuditTrailTechnicalAttributesHandle getGeneralTechnicalAttributesHandle(){
        AuditTrailTechnicalAttributesHandle decoder = new AuditTrailTechnicalAttributesHandle();
        decoder.setMeteringService(inMemoryBootstrapModule.getMeteringService());
        decoder.setNlsService(inMemoryBootstrapModule.getNlsService());
        decoder.setOrmService(inMemoryBootstrapModule.getOrmService());

        return decoder;
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    public static TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    protected UsagePoint createUsagePoint(String name, ServiceKind serviceKind) {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(serviceKind).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint(name, Instant.EPOCH).create();
        return usagePoint;
    }

    protected Optional<UsagePoint> findUsagePointByName(String name){
        return inMemoryBootstrapModule.getMeteringService().findUsagePointByName(name);
    }

    protected void assertAuditTrail(AuditTrail auditTrail, UsagePoint usagePoint, AuditDomainContextType auditDomainContextType, ImmutableMap contextReference,
                                    AuditOperationType operationType){
        Principal principal = inMemoryBootstrapModule.getThreadPrincipalService().getPrincipal();

        assertThat(auditTrail.getOperation()).isEqualTo(operationType);
        assertThat(auditTrail.getTouchDomain().getName()).isEqualTo(usagePoint.getName());
        assertThat(auditTrail.getTouchDomain().getContextReference()).isEqualTo(contextReference);
        assertThat(auditTrail.getDomainContext()).isEqualTo(auditDomainContextType);
        assertThat(auditTrail.getUser()).isEqualTo(principal.getName());
        assertThat(auditTrail.getPkDomain()).isEqualTo(usagePoint.getId());
        assertThat(auditTrail.getPkContext1()).isEqualTo(0);
    }

    protected Optional<AuditTrail>  getLastAuditTrail(AuditService auditService){
        return auditService
                .getAuditTrail(auditService.newAuditTrailFilter(applicationType))
                .stream()
                .filter(auditTrail -> auditTrail.getDomainContext().equals(getDomainContext()))
                .findFirst();
    }

    protected AuditDomainContextType getDomainContext(){
        return AuditDomainContextType.USAGEPOINT_GENERAL_ATTRIBUTES;
    }

    protected static void grantPrivilegesForCurrentUser(){
        User newCurrentUser = mock(User.class);
        when(newCurrentUser.getName()).thenReturn("Test");

        Set<Privilege> privileges = new HashSet<>();
        Privilege editPrivilege = mock(Privilege.class);
        when(editPrivilege.getName()).thenReturn(Privileges.Constants.VIEW_ANY_USAGEPOINT);
        privileges.add(editPrivilege);
        Privilege viewPrivilege = mock(Privilege.class);
        when(viewPrivilege.getName()).thenReturn(Privileges.Constants.ADMINISTER_ANY_USAGEPOINT);
        privileges.add(viewPrivilege);

        when(newCurrentUser.getPrivileges()).thenReturn(privileges);
        when(newCurrentUser.getPrivileges(anyString())).thenReturn(privileges);
        when(newCurrentUser.hasPrivilege(null, Privileges.Constants.VIEW_ANY_USAGEPOINT)).thenReturn(true);
        when(newCurrentUser.hasPrivilege(null, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT)).thenReturn(true);
        inMemoryBootstrapModule.getThreadPrincipalService().set(newCurrentUser);
    }

}
