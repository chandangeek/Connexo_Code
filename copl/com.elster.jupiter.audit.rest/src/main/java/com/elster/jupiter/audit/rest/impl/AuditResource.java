/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrailFilter;
import com.elster.jupiter.audit.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/audit")
public class AuditResource {

    private final TransactionService transactionService;
    private final AuditService auditService;
    private final AuditInfoFactory auditInfoFactory;
    private final AuditLogInfoFactory auditLogInfoFactory;
    private final Thesaurus thesaurus;
    private final UserService userService;

    @Inject
    public AuditResource(TransactionService transactionService,
                         AuditService auditService,
                         UserService userService,
                         AuditInfoFactory auditInfoFactory,
                         AuditLogInfoFactory auditLogInfoFactory,
                         Thesaurus thesaurus) {
        super();
        this.transactionService = transactionService;
        this.auditService = auditService;
        this.userService = userService;
        this.auditInfoFactory = auditInfoFactory;
        this.auditLogInfoFactory = auditLogInfoFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_AUDIT_LOG})
    public PagedInfoList getAuditTrail(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return PagedInfoList.fromPagedList("audit", auditService.getAuditTrail(getAuditTrailFilter(filter))
                .from(queryParameters)
                .stream()
                .map(audit -> auditInfoFactory.from(audit, thesaurus))
                .collect(Collectors.toList()), queryParameters);
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/categories")
    @RolesAllowed({Privileges.Constants.VIEW_AUDIT_LOG})
    public Response getCategories() {
        List<IdWithNameInfo> operationInfos =
                Arrays.stream(AuditDomainType.values())
                        .filter(auditDomainType -> auditDomainType != AuditDomainType.UNKNOWN)
                        .map(auditDomainType ->
                                Arrays.stream(AuditDomainTranslationKeys.values())
                                        .filter(keys -> keys.getKey().compareToIgnoreCase(auditDomainType.type()) == 0)
                                        .findFirst()
                                        .map(key -> new IdWithNameInfo(auditDomainType.type(),
                                                thesaurus.getString(
                                                        AuditDomainTranslationKeys.valueOf(key.name()).getKey(),
                                                        AuditDomainTranslationKeys.valueOf(key.name()).getDefaultFormat())))
                                        .orElseGet(() -> new IdWithNameInfo(auditDomainType.type(), auditDomainType.type())))
                        .collect(Collectors.toList());
        return Response.ok(operationInfos).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/users")
    @RolesAllowed({Privileges.Constants.VIEW_AUDIT_LOG})
    public Response getUsers(@QueryParam("like") String searchText) {
        Condition condition = Condition.TRUE;
        if (!Checks.is(searchText).empty()) {
            condition = condition.and(where("authenticationName").likeIgnoreCase("*" + searchText + "*"));
        }
        List<IdWithNameInfo> users = userService.getUserQuery()
                .select(condition, Order.ascending("authenticationName"))
                .stream()
                .sorted(Comparator.comparing(user -> user.getName().toUpperCase()))
                .map(user -> new IdWithNameInfo(user.getId(), user.getName()))
                .collect(Collectors.toList());
        return Response.ok(users).build();
    }

    private AuditTrailFilter getAuditTrailFilter(JsonQueryFilter filter) {
        AuditTrailFilter auditFilter = auditService.newAuditTrailFilter();
        if (filter.hasProperty("changedOnFrom")) {
            auditFilter.setChangedOnFrom(filter.getInstant("changedOnFrom"));
        }
        if (filter.hasProperty("changedOnTo")) {
            auditFilter.setChangedOnTo(filter.getInstant("changedOnTo"));
        }
        if (filter.hasProperty("categories")) {
            auditFilter.setCategories(filter.getStringList("categories"));
        }
        if (filter.hasProperty("users")) {
            auditFilter.setChangedBy(filter.getStringList("users"));
        }
        return auditFilter;
    }

}