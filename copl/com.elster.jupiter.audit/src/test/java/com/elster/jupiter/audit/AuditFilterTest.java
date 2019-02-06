/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import com.elster.jupiter.audit.impl.AuditServiceImpl;
import com.elster.jupiter.audit.impl.AuditTrailFilterImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditFilterTest {

    private static final Instant CHANGED_ON_FROM = LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    private static final Instant CHANGED_ON_TO = LocalDate.of(2019, 2, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    private static final AuditDomainContextType CONTEXT = AuditDomainContextType.DEVICE_ATTRIBUTES;
    private static final String PRIVILEGE = "PRIVILEGE";
    private static final String CATEGORY = "CATEGORY";
    private static final String USER = "USER";

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private AuditServiceImpl auditService;
    @Mock
    private AuditTrailDecoderHandle auditTrailDecoderHandle;

    @Before
    public void setUp() {

        when(auditService.getAuditTrailDecoderHandles()).thenReturn(Collections.singletonList(auditTrailDecoderHandle));
        when(auditTrailDecoderHandle.getPrivileges()).thenReturn(Collections.singletonList(PRIVILEGE));
        when(auditTrailDecoderHandle.getAuditDomainContextType()).thenReturn(CONTEXT);
        setUpUserPrivileges();
    }

    private void setUpUserPrivileges() {
        User user = mock(User.class);
        Privilege privilege = mock(Privilege.class);
        when(privilege.getName()).thenReturn(PRIVILEGE);
        Set<Privilege> privileges = new HashSet<>();
        privileges.add(privilege);
        when(user.getPrivileges()).thenReturn(privileges);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(threadPrincipalService.getApplicationName()).thenReturn("APPNAME");
        when(user.hasPrivilege(Matchers.matches("APPNAME"), Matchers.<String>anyObject())).thenReturn(true);
    }

    @Test
    public void testAuditFilterWithChangedOn() {
        AuditTrailFilter auditTrailFilter = new AuditTrailFilterImpl(threadPrincipalService, auditService);
        auditTrailFilter.setChangedOnFrom(CHANGED_ON_FROM);
        auditTrailFilter.setChangedOnTo(CHANGED_ON_TO);
        assertThat(auditTrailFilter.toCondition().toString()).isEqualTo(String.format("((context IN [%s]) AND createTime >= ?  AND createTime <= ? )", CONTEXT));
    }

    @Test
    public void testAuditFilterWithCategories() {
        AuditTrailFilter auditTrailFilter = new AuditTrailFilterImpl(threadPrincipalService, auditService);
        auditTrailFilter.setCategories(Collections.singletonList(CATEGORY));
        assertThat(auditTrailFilter.toCondition().toString()).isEqualTo(String.format("((context IN [%s]) AND (domain IN [%s]))", CONTEXT, CATEGORY));
    }

    @Test
    public void testAuditFilterWithChangedBy() {
        AuditTrailFilter auditTrailFilter = new AuditTrailFilterImpl(threadPrincipalService, auditService);
        auditTrailFilter.setChangedBy(Collections.singletonList(USER));
        assertThat(auditTrailFilter.toCondition().toString()).isEqualTo(String.format("((context IN [%s]) AND (userName IN [%s]))", CONTEXT, USER));
    }

    @Test
    public void testZoneFilterWithNullZoneTypes() {
        AuditTrailFilter auditTrailFilter = new AuditTrailFilterImpl(threadPrincipalService, auditService);
        assertThat(auditTrailFilter.toCondition().toString()).isEqualTo(String.format("(context IN [%s])", CONTEXT));
    }

}
