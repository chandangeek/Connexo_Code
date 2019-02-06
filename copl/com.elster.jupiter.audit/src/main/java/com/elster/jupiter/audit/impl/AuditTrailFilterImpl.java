/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrailDecoderHandle;
import com.elster.jupiter.audit.AuditTrailFilter;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditTrailFilterImpl implements AuditTrailFilter {

    private Instant changedOnFrom = Instant.EPOCH;
    private Instant changedOnTo = Instant.EPOCH;
    private List<String> categories = new ArrayList<>();
    private List<String> users = new ArrayList<>();

    private Condition condition = Condition.TRUE;

    public AuditTrailFilterImpl(ThreadPrincipalService threadPrincipalService, AuditService auditService) {
        setContext(threadPrincipalService, auditService);
    }

    @Override
    public Condition toCondition() {
        if (changedOnFrom != Instant.EPOCH) {
            condition = condition.and(Where.where(AuditTrailImpl.Field.CREATETIME.fieldName()).isGreaterThanOrEqual(changedOnFrom));
        }
        if (changedOnTo != Instant.EPOCH) {
            condition = condition.and(Where.where(AuditTrailImpl.Field.CREATETIME.fieldName()).isLessThanOrEqual(changedOnTo));
        }
        if (categories.size() > 0) {
            condition = condition.and(Where.where(AuditTrailImpl.Field.DOMAIN.fieldName()).in(categories));
        }
        if (users.size() > 0) {
            condition = condition.and(Where.where(AuditTrailImpl.Field.USERNAME.fieldName()).in(users));
        }
        return condition;
    }

    @Override
    public void setChangedOnFrom(Instant changedOnFrom) {
        this.changedOnFrom = changedOnFrom;
    }

    @Override
    public void setChangedOnTo(Instant changedOnTo) {
        this.changedOnTo = changedOnTo;
    }

    @Override
    public void setCategories(List<String> categories) {
        this.categories.addAll(categories);
    }

    @Override
    public void setChangedBy(List<String> users) {
        this.users.addAll(users);
    }

    private AuditTrailFilter setContext(ThreadPrincipalService threadPrincipalService, AuditService auditService) {

        List<String> domainContexts = ((AuditServiceImpl) auditService).getAuditTrailDecoderHandles().stream()
                .filter(auditTrailDecoderHandle ->
                        hasAtLeastOnePrivileges(auditTrailDecoderHandle.getPrivileges(), threadPrincipalService))
                .map(AuditTrailDecoderHandle::getContext)
                .collect(Collectors.toList());
        condition = condition.and(Where.where(AuditTrailImpl.Field.CONTEXT.fieldName()).in(domainContexts));
        return this;
    }

    private boolean hasAtLeastOnePrivileges(List<String> privileges, ThreadPrincipalService threadPrincipalService) {
        return privileges.stream()
                .filter(privilege -> ((User) (threadPrincipalService.getPrincipal())).hasPrivilege(threadPrincipalService.getApplicationName(), privilege))
                .count() > 0;
    }
}
