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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.rest.MessageSeeds;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
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
//    private final ExceptionFactory exceptionFactory;

    @Inject
    public TrustStoreResource(PkiService pkiService/*, ExceptionFactory exceptionFactory*/) {
        this.pkiService = pkiService;
//        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getTrustStores(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList("trustStores", wrapTrustStores(this.pkiService.getAllTrustStores()), queryParameters);
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public TrustStoreInfo getTrustStore(@PathParam("name") String name) {
        Optional<TrustStore> trustStore = this.pkiService.findTrustStore(name);
        if (trustStore.isPresent()) {
            return new TrustStoreInfo(trustStore.get());
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
        return new TrustStoreInfo(trustStore);
    }

    @PUT
    @Transactional
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response editTrustStore(@PathParam("name") String name, TrustStoreInfo info) {
        Optional<TrustStore> trustStore = pkiService.findTrustStore(name);
//                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_TRUSTSTORE));

//        info.id = id;
//        DeviceLifeCycle deviceLifeCycle = resourceHelper.lockDeviceLifeCycleOrThrowException(info);
//        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
//        deviceLifeCycleUpdater.setName(info.name).complete().save();
//        return Response.ok(deviceLifeCycleFactory.from(deviceLifeCycle)).build();
          return Response.ok(new TrustStoreInfo(trustStore.get())).build();
    }

    @DELETE
    @Transactional
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response deleteTrustStore(@PathParam("name") String name) {
//        pkiService.findTrustStore(name)
//                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_TRUSTSTORE))
//                .makeObsolete();
        return Response.status(Response.Status.OK).build();
    }


    private List<TrustStoreInfo> wrapTrustStores(List<TrustStore> trustStores) {
        return trustStores != null
            ? trustStores.stream().filter(Objects::nonNull).map(TrustStoreInfo::new).collect(Collectors.toList())
            : Collections.emptyList();
    }
}
