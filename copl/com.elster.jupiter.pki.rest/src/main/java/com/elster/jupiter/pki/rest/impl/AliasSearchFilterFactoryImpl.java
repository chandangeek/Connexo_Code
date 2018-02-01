/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.rest.AliasSearchFilterFactory;
import com.elster.jupiter.util.Checks;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.pki.rest.impl.AliasSearchFilterFactoryImpl",
        service = {AliasSearchFilterFactory.class},
        immediate = true)
public class AliasSearchFilterFactoryImpl implements AliasSearchFilterFactory {
    private static final String ALIAS = "alias";
    private static final String TRUST_STORE = "trustStore";

    private volatile SecurityManagementService securityManagementService;

    public AliasSearchFilterFactoryImpl() {
        // for OSGI
    }

    @Inject
    public AliasSearchFilterFactoryImpl(SecurityManagementService securityManagementService) {
        setSecurityManagementService(securityManagementService);
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Override
    public SecurityManagementService.AliasSearchFilter from(UriInfo uriInfo) {
        SecurityManagementService.AliasSearchFilter aliasSearchFilter = new SecurityManagementService.AliasSearchFilter();
        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        aliasSearchFilter.alias = Optional.ofNullable(uriParams.get(ALIAS))
                .map(List::stream)
                .flatMap(Stream::findFirst)
                .filter(value -> !Checks.is(value).empty())
                .map(value -> value.contains("*") || value.contains("?") ? value : '*' + value + '*')
                .orElse("*");
        Optional.ofNullable(uriParams.get(TRUST_STORE))
                .map(List::stream)
                .flatMap(Stream::findFirst)
                .map(Long::valueOf)
                .map(id -> securityManagementService.findTrustStore(id)
                        .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUSTSTORE, TRUST_STORE)))
                .ifPresent(trustStore -> aliasSearchFilter.trustStore = trustStore);
        return aliasSearchFilter;
    }
}
