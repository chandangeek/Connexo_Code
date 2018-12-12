/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;


/**
 * Created by bvn on 3/29/17.
 */
@Path("/keytypes")
public class KeyTypeResource {

    private final SecurityManagementService securityManagementService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public KeyTypeResource(SecurityManagementService securityManagementService, ExceptionFactory exceptionFactory) {
        this.securityManagementService = securityManagementService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getKeyTypes(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter jsonQueryFilter) {
        Predicate<? super KeyType> cryptoTypeFilter = keyType -> true;
        if (jsonQueryFilter.hasFilters()) {
                cryptoTypeFilter = keyType -> jsonQueryFilter.getStringList("CryptographicType")
                        .stream()
                        .map(CryptographicType::valueOf)
                        .anyMatch(ct -> ct.equals(keyType.getCryptographicType()));
        }
        try {
            List<IdWithNameInfo> collect = securityManagementService.getKeyTypes().stream().filter(cryptoTypeFilter).map(IdWithNameInfo::new).collect(toList());
            return PagedInfoList.fromCompleteList("keyTypes", collect, queryParameters);
        } catch (IllegalArgumentException e) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CRYPTOGRAPHIC_TYPE);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/forCsrCreation")
    public PagedInfoList getClientCertificateKeyTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> collect = securityManagementService.getKeyTypes()
                .stream()
                .filter(kt -> EnumSet.of(CryptographicType.ClientCertificate).contains(kt.getCryptographicType()))
                .map(IdWithNameInfo::new)
                .collect(toList());
        return PagedInfoList.fromCompleteList("keyTypes", collect, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/asymmetric")
    public PagedInfoList getAssymetricKeyTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> collect = securityManagementService.getKeyTypes()
                .stream()
                .filter(kt -> EnumSet.of(CryptographicType.ClientCertificate).contains(kt.getCryptographicType()))
                .map(IdWithNameInfo::new)
                .collect(toList());
        return PagedInfoList.fromCompleteList("keyTypes", collect, queryParameters);
    }

}
