/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest;

import com.elster.jupiter.pki.SecurityManagementService;

import aQute.bnd.annotation.ProviderType;

import javax.ws.rs.core.UriInfo;

@ProviderType
public interface AliasSearchFilterFactory {
    SecurityManagementService.AliasSearchFilter from(UriInfo uriInfo);
}
