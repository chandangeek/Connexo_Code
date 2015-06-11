package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Created by Lucian on 5/14/2015.
 */


@Path("/importers")
public class FileImportersResource {

    private final FileImportService fileImportService;
    private final RestQueryService queryService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final PropertyUtils propertyUtils;

    @Inject
    public FileImportersResource(RestQueryService queryService, FileImportService fileImportService, Thesaurus thesaurus, TransactionService transactionService, PropertyUtils propertyUtils) {
        this.queryService = queryService;
        this.fileImportService = fileImportService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.propertyUtils = propertyUtils;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES})
    public Response getImporters(@Context UriInfo uriInfo, @QueryParam("application") String applicationName) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());

        List<FileImporterFactory> importers = fileImportService.getAvailableImporters(applicationName);
        FileImporterInfos infos = new FileImporterInfos(params.clipToLimit(importers), thesaurus, propertyUtils);
        infos.total = params.determineTotal(importers.size());
        return Response.ok(infos).build();
    }

}
