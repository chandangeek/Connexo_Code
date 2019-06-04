/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest;


import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditReference;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import com.jayway.jsonpath.JsonModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuditResourceTest extends AuditApplicationJerseyTest {
    static final String X_CONNEXO_APPLICATION_NAME = "X-CONNEXO-APPLICATION-NAME";

    private static final String USER_NAME_A = "ABC";
    private static final String USER_NAME_B = "BCD";
    private static final int USER_ID_A = 1;
    private static final int USER_ID_B = 2;

    private static final AuditOperationType AUDIT_TRAIL_OPERATION = AuditOperationType.UPDATE;
    private static final Instant AUDIT_TRAIL_CHANGEDON = LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    private static final AuditDomainType AUDIT_TRAIL_DOMAIN = AuditDomainType.DEVICE;
    private static final AuditDomainContextType AUDIT_TRAIL_DOMAIN_CONTEXT = AuditDomainContextType.GENERAL_ATTRIBUTES;
    private static final String AUDIT_TRAIL_USER = "USR";
    private static final String AUDIT_REFERNCE_NAME = "DEVICE";
    private static final String AUDIT_REFERNCE_REFERENCE = "1";

    private static final String AUDIT_LOG_NAME = "Device name";
    private static final String AUDIT_LOG_VALUE = "1";
    private static final String AUDIT_LOG_PREVIOUS_VALUE = "2";
    private static final String AUDIT_LOG_TYPE = "String";

    @Before
    public void setUp1() {
        AuditTrail auditTrail = mockAuditTrail(1L, AUDIT_TRAIL_OPERATION, AUDIT_TRAIL_CHANGEDON, AUDIT_TRAIL_DOMAIN_CONTEXT, AUDIT_TRAIL_USER);

        Finder<AuditTrail> zoneFinder = mockFinder(Arrays.asList(auditTrail));
        when(auditService.getAuditTrail(any())).thenReturn(zoneFinder);

        // users
        List<User> users = Arrays.asList(mockUser(USER_ID_B, USER_NAME_B), mockUser(USER_ID_A, USER_NAME_A));
        Query<User> queryUser = mock(Query.class);
        when(queryUser.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(users);
        when(userService.getUserQuery()).thenReturn(queryUser);
    }

    private AuditTrail mockAuditTrail(Long id, AuditOperationType operation, Instant changedOn, AuditDomainContextType domainContext, String user) {
        AuditTrail auditTrail = mock(AuditTrail.class);
        when(auditTrail.getId()).thenReturn(id);
        when(auditTrail.getOperation()).thenReturn(operation);
        when(auditTrail.getChangedOn()).thenReturn(changedOn);
        when(auditTrail.getDomainContext()).thenReturn(domainContext);
        when(auditTrail.getUser()).thenReturn(user);

        AuditReference auditReference = mockTouchDomain();
        List<AuditLogChange> auditLogChanges = mockAuditLogs();
        when(auditTrail.getTouchDomain()).thenReturn(auditReference);
        when(auditTrail.getLogs()).thenReturn(auditLogChanges);

        return auditTrail;
    }

    private AuditReference mockTouchDomain() {
        AuditReference auditReference = mock(AuditReference.class);
        when(auditReference.getName()).thenReturn(AUDIT_REFERNCE_NAME);
        when(auditReference.getContextReference()).thenReturn(AUDIT_REFERNCE_REFERENCE);
        return auditReference;
    }

    private List<AuditLogChange> mockAuditLogs() {
        AuditLogChange auditLogChange = mock(AuditLogChange.class);
        when(auditLogChange.getName()).thenReturn(AUDIT_LOG_NAME);
        when(auditLogChange.getValue()).thenReturn(AUDIT_LOG_VALUE);
        when(auditLogChange.getPreviousValue()).thenReturn(AUDIT_LOG_PREVIOUS_VALUE);
        when(auditLogChange.getType()).thenReturn(AUDIT_LOG_TYPE);
        return Arrays.asList(auditLogChange);
    }

    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        return user;
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(com.elster.jupiter.domain.util.QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

    @Test
    public void testGetAudits() {
        String json = target("audit").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.audit[0].domainType")).isEqualTo(AUDIT_TRAIL_DOMAIN.name());
        assertThat(jsonModel.<String>get("$.audit[0].domain")).isEqualTo("Device");

        assertThat(jsonModel.<String>get("$.audit[0].domainType")).isEqualTo(AUDIT_TRAIL_DOMAIN.type());
        assertThat(jsonModel.<String>get("$.audit[0].contextType")).isEqualTo(AUDIT_TRAIL_DOMAIN_CONTEXT.name());
        //assertThat(jsonModel.<Instant>get("$.audit[0].changedOn")).isEqualTo(Date.from(AUDIT_TRAIL_CHANGEDON));
        assertThat(jsonModel.<String>get("$.audit[0].operation")).isEqualTo("Changed attributes");
        assertThat(jsonModel.<String>get("$.audit[0].operationType")).isEqualTo(AUDIT_TRAIL_OPERATION.type());
        assertThat(jsonModel.<String>get("$.audit[0].user")).isEqualTo(AUDIT_TRAIL_USER);

        assertThat(jsonModel.<String>get("$.audit[0].auditReference.name")).isEqualTo(AUDIT_REFERNCE_NAME);
        assertThat(jsonModel.<String>get("$.audit[0].auditReference.contextReference")).isEqualTo(AUDIT_REFERNCE_REFERENCE);
        assertThat(jsonModel.<String>get("$.audit[0].auditLogs[0].name")).isEqualTo(AUDIT_LOG_NAME);
        assertThat(jsonModel.<String>get("$.audit[0].auditLogs[0].value")).isEqualTo(AUDIT_LOG_VALUE);
        assertThat(jsonModel.<String>get("$.audit[0].auditLogs[0].previousValue")).isEqualTo(AUDIT_LOG_PREVIOUS_VALUE);
        assertThat(jsonModel.<String>get("$.audit[0].auditLogs[0].type")).isEqualTo(AUDIT_LOG_TYPE);
    }

    @Test
    public void testGetCategories() {
        List<IdWithNameInfo> categories = target("audit/categories").request().header(X_CONNEXO_APPLICATION_NAME, "MDC").get(List.class);
        assertThat(categories.size()).isEqualTo(1);
        assertThat(((Map) categories.get(0)).get("name")).isEqualTo("Device");
        assertThat(((Map) categories.get(0)).get("id")).isEqualTo("DEVICE");
    }

    @Test
    public void testGetUsers() {
        List<IdWithNameInfo> users = target("audit/users").request().get(List.class);
        assertThat(users.size()).isEqualTo(2);
        assertThat(((Map) users.get(0)).get("name")).isEqualTo(USER_NAME_A);
        assertThat(((Map) users.get(0)).get("id")).isEqualTo(USER_ID_A);
        assertThat(((Map) users.get(1)).get("name")).isEqualTo(USER_NAME_B);
        assertThat(((Map) users.get(1)).get("id")).isEqualTo(USER_ID_B);
    }
}
