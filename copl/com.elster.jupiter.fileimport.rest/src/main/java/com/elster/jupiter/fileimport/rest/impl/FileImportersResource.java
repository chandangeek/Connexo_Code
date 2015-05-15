package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @Inject
    public FileImportersResource(RestQueryService queryService, FileImportService fileImportService, Thesaurus thesaurus, TransactionService transactionService) {
        this.queryService = queryService;
        this.fileImportService = fileImportService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    //@RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES})
    public Response getImporters(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());

        List<FileImporterFactory> importers = fileImportService.getAvailableImporters();
        FileImporterInfos infos = new FileImporterInfos(params.clipToLimit(importers), thesaurus);
        infos.total = params.determineTotal(importers.size());

        return Response.ok(infos).build();
    }

    private List<? extends ImportSchedule> queryImportSchedules(QueryParameters queryParameters) {
        Query<ImportSchedule> query = fileImportService.getImportSchedulesQuery();
        RestQuery<ImportSchedule> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }
}
