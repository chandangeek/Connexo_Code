/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrailDecoderHandle;
import com.elster.jupiter.audit.AuditTrailFilter;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableAudit;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AuditTrailFilterImpl implements AuditTrailFilter {

    private Instant changedOnFrom = Instant.EPOCH;
    private Instant changedOnTo = Instant.EPOCH;
    private List<String> categories = new ArrayList<>();
    private List<String> users = new ArrayList<>();

    private Condition condition = Condition.TRUE;

    AuditTrailFilterImpl(OrmService ormService, ThreadPrincipalService threadPrincipalService, AuditService auditService) {
        setContext(ormService, threadPrincipalService, auditService);
    }

    @Override
    public Condition toCondition() {
        Condition condition = Condition.TRUE;
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

    private AuditTrailFilter setContext(OrmService ormService, ThreadPrincipalService threadPrincipalService, AuditService auditService) {
        List<TableAudit> tableAudits = ormService.getDataModels().stream()
                .map(DataModel::getTables)
                .flatMap(Collection::stream)
                .filter(Table::hasAudit)
                .map(Table::getTableAudit)
                .collect(Collectors.toList());

        List<String> domainContexts = tableAudits.stream()
                .filter(tableAudit ->
                        hasAtLeastOnePrivileges(((AuditServiceImpl) auditService).getAuditTrailDecoderHandles(tableAudit.getDomain(), tableAudit.getContext())
                                .map(AuditTrailDecoderHandle::getPrivileges)
                                .orElse(Collections.emptyList()), threadPrincipalService))
                .map(TableAudit::getContext)
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
