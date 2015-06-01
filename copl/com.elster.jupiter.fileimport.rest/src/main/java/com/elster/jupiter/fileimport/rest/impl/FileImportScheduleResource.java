package com.elster.jupiter.fileimport.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.fileimport.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.cron.CronExpressionParser;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Path("/importservices")
public class FileImportScheduleResource {

    private final FileImportService fileImportService;
    private final RestQueryService queryService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final CronExpressionParser cronExpressionParser;
    private final PropertyUtils propertyUtils;


    @Inject
    public FileImportScheduleResource(RestQueryService queryService, FileImportService fileImportService, Thesaurus thesaurus, TransactionService transactionService, CronExpressionParser cronExpressionParser, PropertyUtils propertyUtils) {
        this.queryService = queryService;
        this.fileImportService = fileImportService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.cronExpressionParser = cronExpressionParser;
        this.propertyUtils = propertyUtils;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public Response getImportSchedules(@Context UriInfo uriInfo, @QueryParam("application") String applicationName) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<ImportSchedule> list = queryImportSchedules(params)
                .stream()
                .filter(is -> applicationName != null && ("SYS".equals(applicationName) || applicationName.equals(is.getApplicationName())))
                .collect(Collectors.toList());

        FileImportScheduleInfos infos = new FileImportScheduleInfos(params.clipToLimit(list), thesaurus, propertyUtils);
        infos.total = params.determineTotal(list.size());

        return Response.ok(infos).build();
    }

    private List<ImportSchedule> queryImportSchedules(QueryParameters queryParameters) {
        Query<ImportSchedule> query = fileImportService.getImportSchedulesQuery();
        RestQuery<ImportSchedule> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }


    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public FileImportScheduleInfo getImportSchedule(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return new FileImportScheduleInfo(fetchImportSchedule(id), thesaurus, propertyUtils);
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES})
    public Response addImportSchedule(FileImportScheduleInfo info) {
        ImportScheduleBuilder builder = fileImportService.newBuilder()
                .setName(info.name)
                .setDestination(fileImportService.getImportFactory(info.importerInfo.name).get().getDestinationName())
                .setPathMatcher(info.pathMatcher)
                .setImportDirectory(new File(info.importDirectory))
                .setFailureDirectory(new File(info.failureDirectory))
                .setSuccessDirectory(new File(info.successDirectory))
                .setProcessingDirectory(new File(info.inProcessDirectory))
                .setImportDirectory(new File(info.importDirectory))
                .setImporterName(info.importerInfo.name)
                .setScheduleExpression(ScanFrequency.toScheduleExpression(info.scanFrequency, cronExpressionParser));
                //.setScheduleExpression(getScheduleExpression(info));
                //.setCronExpression(ScanFrequency.toScheduleExpression(info.scanFrequency,cronExpressionParser));


        List<PropertySpec> propertiesSpecs = fileImportService.getPropertiesSpecsForImporter(info.importerInfo.name);

        propertiesSpecs.stream()
                .forEach(spec -> {
                    Object value = propertyUtils.findPropertyValue(spec, info.properties);
                    builder.addProperty(spec.getName()).withValue(value);
                });


        ImportSchedule importSchedule = builder.build();
        try (TransactionContext context = transactionService.getContext()) {
            importSchedule.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new FileImportScheduleInfo(importSchedule, thesaurus, propertyUtils)).build();
    }

    @DELETE
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES})
    public Response removeImportSchedule(@PathParam("id") long id, @Context SecurityContext securityContext) {
        ImportSchedule importSchedule = fetchImportSchedule(id);

        try (TransactionContext context = transactionService.getContext()) {
            importSchedule.delete();
            context.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.DELETE_IMPORT_SCHEDULE_SQL_EXCEPTION, "status", importSchedule.getName());
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES})
    public Response updateImportSchedule(@PathParam("id") long id, FileImportScheduleInfo info) {

        ImportSchedule importSchedule = fetchImportSchedule(info.id);

        try (TransactionContext context = transactionService.getContext()) {
            importSchedule.setName(info.name);
            importSchedule.setActive(info.active);
            importSchedule.setDestination(fileImportService.getImportFactory(info.importerInfo.name).get().getDestinationName());
            importSchedule.setImportDirectory(new File(info.importDirectory));
            importSchedule.setFailureDirectory(new File(info.failureDirectory));
            importSchedule.setSuccessDirectory(new File(info.successDirectory));
            importSchedule.setProcessingDirectory(new File(info.inProcessDirectory));
            importSchedule.setImporterName(info.importerInfo.name);
            importSchedule.setPathMatcher(info.pathMatcher);
            //importSchedule.setScheduleExpression(getScheduleExpression(info));
            importSchedule.setScheduleExpression(ScanFrequency.toScheduleExpression(info.scanFrequency, cronExpressionParser));

            updateProperties(info, importSchedule);

            importSchedule.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new FileImportScheduleInfo(importSchedule, thesaurus, propertyUtils)).build();
    }

    /*private ScheduleExpression getScheduleExpression(FileImportScheduleInfo info) {
        if(info.schedule == null){
            info.schedule = new PeriodicalExpressionInfo();
            info.schedule.offsetSeconds = 0;
            info.schedule.offsetMinutes = 0;
            info.schedule.offsetHours = 0;
            info.schedule.offsetDays = 0;
            info.schedule.offsetMonths = 0;
            info.schedule.lastDayOfMonth = false;
            info.schedule.dayOfWeek = null;
            info.schedule.count = info.scanFrequency;
            info.schedule.timeUnit = PeriodicalScheduleExpression.Period.MINUTE.getIdentifier();
        }
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
    }*/

    private void updateProperties(FileImportScheduleInfo info, ImportSchedule importSchedule) {
        List<PropertySpec> propertiesSpecs = fileImportService.getPropertiesSpecsForImporter(info.importerInfo.name);
        propertiesSpecs.stream()
                .forEach(spec -> {
                    Object value = propertyUtils.findPropertyValue(spec, info.properties);
                    importSchedule.setProperty(spec.getName(), value);
                });
    }


    private ImportSchedule fetchImportSchedule(long id) {
        return fileImportService.getImportSchedule(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }


}
