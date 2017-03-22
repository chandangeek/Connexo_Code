/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.rest.MessageSeeds;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/truststores")
public class TrustStoreResource {

    private final PkiService pkiService;
    private final TrustStoreInfoFactory trustStoreInfoFactory;
    private final TrustedCertificateInfoFactory trustedCertificateInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public TrustStoreResource(PkiService pkiService, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory, TrustStoreInfoFactory trustStoreInfoFactory, TrustedCertificateInfoFactory trustedCertificateInfoFactory) {
        this.pkiService = pkiService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.trustStoreInfoFactory = trustStoreInfoFactory;
        this.trustedCertificateInfoFactory = trustedCertificateInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getTrustStores(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList("trustStores", trustStoreInfoFactory.asInfoList(this.pkiService.getAllTrustStores()), queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public TrustStoreInfo getTrustStore(@PathParam("id") long id) {
        Optional<TrustStore> trustStore = this.pkiService.findTrustStore(id);
        if (trustStore.isPresent()) {
            return trustStoreInfoFactory.asInfo(trustStore.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/{id}/certificates")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCertificates(@PathParam("id") long id, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Optional<TrustStore> trustStore = this.pkiService.findTrustStore(id);
        if (trustStore.isPresent()) {
            return asPagedInfoList(trustedCertificateInfoFactory.asInfo(trustStore.get(), uriInfo), "certificates", queryParameters);
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public TrustStoreInfo addTrustStore(TrustStoreInfo info) {
        PkiService.TrustStoreBuilder builder = pkiService.newTrustStore(info.name);
        if (info.description != null) {
            builder = builder.description(info.description);
        }
        TrustStore trustStore = builder.add();
        return trustStoreInfoFactory.asInfo(trustStore);
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response editTrustStore(@PathParam("id") long id, TrustStoreInfo info) {
        TrustStore trustStore = pkiService.findAndLockTrustStoreByIdAndVersion(id, info.version)
            .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                    .withActualVersion(() -> getCurrentTrustStoreVersion(info.id))
                    .supplier());
        trustStore.setName(info.name);
        trustStore.setDescription(info.description);
        trustStore.save();
        return Response.ok(trustStoreInfoFactory.asInfo(trustStore)).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response deleteTrustStore(@PathParam("id") long id) {
//        pkiService.findTrustStore(id)
//                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_TRUSTSTORE));
//                .delete();
        return Response.status(Response.Status.OK).build();
    }

    public Long getCurrentTrustStoreVersion(long id) {
        return pkiService.findTrustStore(id).map(TrustStore::getVersion).orElse(null);
    }

    private PagedInfoList asPagedInfoList(List<TrustedCertificateInfo> trustedCertificateInfos, String rootKeyName, JsonQueryParameters queryParameters) {
        List<TrustedCertificateInfo> pagedInfos = ListPager.of(trustedCertificateInfos).from(queryParameters).find();
        return PagedInfoList.fromPagedList(rootKeyName, pagedInfos, queryParameters);
    }

}
