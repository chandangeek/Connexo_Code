/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditDecoderHandle;
import com.elster.jupiter.audit.AuditFilter;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableAudit;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AuditFilterImpl implements AuditFilter {

    private Condition condition = Condition.TRUE;

    AuditFilterImpl(OrmService ormService, ThreadPrincipalService threadPrincipalService, AuditService auditService) {
        setContext(ormService, threadPrincipalService, auditService);
    }

    @Override
    public Condition toCondition() {
        return condition;
    }

    private AuditFilter setContext(OrmService ormService, ThreadPrincipalService threadPrincipalService, AuditService auditService) {
        List<TableAudit> tableAudits = ormService.getDataModels().stream()
                .map(DataModel::getTables)
                .flatMap(Collection::stream)
                .filter(Table::hasAudit)
                .map(Table::getTableAudit)
                .collect(Collectors.toList());

        List<String> domainContexts = tableAudits.stream()
                .filter(tableAudit ->
                        hasAtLeastOnePrivileges(((AuditServiceImpl) auditService).getAuditDecoderHandles(tableAudit.getTable().getName())
                                .map(AuditDecoderHandle::getPrivileges)
                                .orElse(Collections.emptyList()), threadPrincipalService))
                .map(TableAudit::getContext)
                .collect(Collectors.toList());
        condition = condition.and(Where.where(AuditImpl.Field.CONTEXT.fieldName()).in(domainContexts));
        return this;
    }

    private boolean hasAtLeastOnePrivileges(List<String> privileges, ThreadPrincipalService threadPrincipalService) {
        return privileges.stream()
                .filter(privilege -> ((User) (threadPrincipalService.getPrincipal())).hasPrivilege(threadPrincipalService.getApplicationName(), privilege))
                .count() > 0;
    }
}
