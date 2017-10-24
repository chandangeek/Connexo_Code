/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.device.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This class is duplicated here because of issues with Privileges
 */
@Path("/truststores")
public class TrustStoreResource {

    private final SecurityManagementService securityManagementService;

    @Inject
    public TrustStoreResource(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public PagedInfoList getTrustStores(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> list = this.securityManagementService.getAllTrustStores().stream().map(IdWithNameInfo::new).collect(toList());
        return PagedInfoList.fromCompleteList("trustStores", list, queryParameters);
    }

}
