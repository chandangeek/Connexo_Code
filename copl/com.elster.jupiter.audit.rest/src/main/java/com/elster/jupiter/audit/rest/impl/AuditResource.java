/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditFilter;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.rest.AuditInfoFactory;
import com.elster.jupiter.audit.rest.AuditLogInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.stream.Collectors;

@Path("/audit")
public class AuditResource {

    private final TransactionService transactionService;
    private final AuditService auditService;
    private final AuditInfoFactory auditInfoFactory;
    private final AuditLogInfoFactory auditLogInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public AuditResource(TransactionService transactionService,
                         AuditService auditService,
                         AuditInfoFactory auditInfoFactory,
                         AuditLogInfoFactory auditLogInfoFactory,
                         Thesaurus thesaurus) {
        super();
        this.transactionService = transactionService;
        this.auditService = auditService;
        this.auditInfoFactory = auditInfoFactory;
        this.auditLogInfoFactory = auditLogInfoFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    // @RolesAllowed({Privileges.Constants.AUDIT})
    public PagedInfoList getAudit(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return PagedInfoList.fromPagedList("audit", auditService.getAudit(getAuditFilter(filter))
                .from(queryParameters)
                .stream()
                .map(audit -> auditInfoFactory.from(audit, auditLogInfoFactory))
                .collect(Collectors.toList()), queryParameters);
    }

    private AuditFilter getAuditFilter(JsonQueryFilter filter) {
        AuditFilter auditFilter = auditService.newAuditFilter();
        return auditFilter;
    }

}