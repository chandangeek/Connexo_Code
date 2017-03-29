/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 3/29/17.
 */
@Path("keyencryptionmethods")
public class KeyEncryptionMethodResource {
    private final PkiService pkiService;

    @Inject
    public KeyEncryptionMethodResource(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/asymmetric")
    public PagedInfoList getKeyEncryptionMethodsForAsymmetricKeys(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> collect = pkiService.getKeyEncryptionMethods(CryptographicType.AsymmetricKey)
                .stream()
                .map(kem -> new IdWithNameInfo(kem, kem))
                .collect(toList());
        return PagedInfoList.fromCompleteList("keyEncryptionMethods", collect, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/symmetric")
    public PagedInfoList getKeyEncryptionMethodsForSymmetricKeys(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> collect = pkiService.getKeyEncryptionMethods(CryptographicType.SymmetricKey)
                .stream()
                .map(kem -> new IdWithNameInfo(kem, kem))
                .collect(toList());
        return PagedInfoList.fromCompleteList("keyEncryptionMethods", collect, queryParameters);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/passphrase")
    public PagedInfoList getKeyEncryptionMethodsForPassphrases(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> collect = pkiService.getKeyEncryptionMethods(CryptographicType.Passphrase)
                .stream()
                .map(kem -> new IdWithNameInfo(kem, kem))
                .collect(toList());
        return PagedInfoList.fromCompleteList("keyEncryptionMethods", collect, queryParameters);
    }
}
