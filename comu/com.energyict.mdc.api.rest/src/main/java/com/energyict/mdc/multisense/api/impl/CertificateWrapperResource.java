/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/22/15.
 */
@Path("/certificates")
public class CertificateWrapperResource {

    private final SecurityManagementService securityManagementService;
    private final CertificateWrapperInfoFactory certificateWrapperInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CertificateWrapperResource(SecurityManagementService securityManagementService, CertificateWrapperInfoFactory certificateWrapperInfoFactory, ExceptionFactory exceptionFactory) {
        this.securityManagementService = securityManagementService;
        this.certificateWrapperInfoFactory = certificateWrapperInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * @param id Id of the certificate wrapper
     * @param uriInfo uriInfo
     * @param fields fields
     * @return Uniquely identofied certificate wrapper
     * @summary Fetch certificate wrapper by id
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{certificateWrapperId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public CertificateWrapperInfo getCertificateWrapper(@PathParam("certificateWrapperId") long id, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        return securityManagementService.findCertificateWrapper(id)
                .map(d -> certificateWrapperInfoFactory.from(d, uriInfo, fields.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_CERTIFCATE_WRAPPER));
    }

    /**
     * @param alias Alias name of the certificate wrapper
     * @param uriInfo uriInfo
     * @param fields fields
     * @return Uniquely identofied certificate wrapper
     * @summary Fetch certificate wrapper by alias name
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/alias/{certificateWrapperAlias}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public CertificateWrapperInfo getCertificateWrapperByAlias(@PathParam("certificateWrapperAlias") String alias, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        return securityManagementService.findCertificateWrapper(alias)
                .map(d -> certificateWrapperInfoFactory.from(d, uriInfo, fields.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_CERTIFCATE_WRAPPER));
    }

    /**
     * @param id The certificate wrapper identifier
     * @param uriInfo uriInfo
     * @return No content response
     * @summary Deletes a specific certificate wrapper
     */
    @DELETE
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    @Path("/{certificateWrapperId}")
    public Response deleteCertificateWrapper(@PathParam("certificateWrapperId") long id,
                                             @Context UriInfo uriInfo) {
        securityManagementService.findCertificateWrapper(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_CERTIFCATE_WRAPPER))
                .delete();
        return Response.noContent().build();
    }


    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     * @summary List the fields available on this type of entity
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return certificateWrapperInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}

