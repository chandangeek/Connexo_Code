package com.elster.jupiter.fileimport.rest.impl;


import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.fileimport.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.nio.file.FileSystem;
import java.time.Instant;
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
    private final FileSystem fileSystem;
    private final AppService appService;


    @Inject
    public FileImportScheduleResource(RestQueryService queryService, FileImportService fileImportService, Thesaurus thesaurus, TransactionService transactionService, CronExpressionParser cronExpressionParser, PropertyUtils propertyUtils, FileSystem fileSystem, AppService appService) {
        this.queryService = queryService;
        this.fileImportService = fileImportService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.cronExpressionParser = cronExpressionParser;
        this.propertyUtils = propertyUtils;
        this.fileSystem = fileSystem;
        this.appService = appService;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public PagedInfoList getImportSchedules(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @QueryParam("application") String applicationName) {


        List<ImportSchedule> list = fileImportService.findImportSchedules(applicationName).from(queryParameters).find();
        List<FileImportScheduleInfo> data = list.stream().map(each -> new FileImportScheduleInfo(each, appService, thesaurus, propertyUtils)).collect(Collectors.toList());

        return PagedInfoList.fromPagedList("importSchedules",data,queryParameters);
    }

    @GET
    @Path("/list/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public PagedInfoList getImportSchedulesList(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @QueryParam("application") String applicationName) {

        List<ImportSchedule> list = fileImportService.findAllImportSchedules(applicationName).from(queryParameters).find();
        List<ImportServiceNameInfo> data = list.stream().map(each -> new ImportServiceNameInfo(each, thesaurus)).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("importSchedules",data,queryParameters);
    }


    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public FileImportScheduleInfo getImportSchedule(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return new FileImportScheduleInfo(fetchImportSchedule(id), appService, thesaurus, propertyUtils);
    }

    @GET
    @Path("/list/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public FileImportScheduleInfo getImportScheduleIncludeDeleted(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return new FileImportScheduleInfo(fetchImportScheduleIncludeDeleted(id), appService, thesaurus, propertyUtils);
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES})
    public Response addImportSchedule(FileImportScheduleInfo info) {
        ImportScheduleBuilder builder = fileImportService.newBuilder()
                .setName(info.name)
                .setPathMatcher(info.pathMatcher)
                .setImportDirectory(fileSystem.getPath(info.importDirectory))
                .setFailureDirectory(fileSystem.getPath(info.failureDirectory))
                .setSuccessDirectory(fileSystem.getPath(info.successDirectory))
                .setProcessingDirectory(fileSystem.getPath(info.inProcessDirectory))
                .setImporterName(info.importerInfo.name)
                .setScheduleExpression(ScanFrequency.toScheduleExpression(info.scanFrequency, cronExpressionParser));


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
        return Response.status(Response.Status.CREATED).entity(new FileImportScheduleInfo(importSchedule, appService, thesaurus, propertyUtils)).build();
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
        if(!importSchedule.isImporterAvailable()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        try (TransactionContext context = transactionService.getContext()) {
            importSchedule.setName(info.name);
            importSchedule.setActive(info.active);
            importSchedule.setImportDirectory(fileSystem.getPath(info.importDirectory));
            importSchedule.setFailureDirectory(fileSystem.getPath(info.failureDirectory));
            importSchedule.setSuccessDirectory(fileSystem.getPath(info.successDirectory));
            importSchedule.setProcessingDirectory(fileSystem.getPath(info.inProcessDirectory));
            importSchedule.setImporterName(info.importerInfo.name);
            importSchedule.setPathMatcher(info.pathMatcher);
            importSchedule.setScheduleExpression(ScanFrequency.toScheduleExpression(info.scanFrequency, cronExpressionParser));
            updateProperties(info, importSchedule);

            importSchedule.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new FileImportScheduleInfo(importSchedule, appService, thesaurus, propertyUtils)).build();
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public PagedInfoList getImportScheduleOccurrences(@BeanParam JsonQueryParameters queryParameters,
                                                                      @BeanParam JsonQueryFilter filter,
                                                                      @PathParam("id") long importServiceId,
                                                                      @QueryParam("application") String applicationName,
                                                                      @Context SecurityContext securityContext) {

        List<FileImportOccurrence> fileImportOccurences = getFileImportOccurrences(queryParameters, filter, applicationName, importServiceId);
        List<FileImportOccurrenceInfo> data = fileImportOccurences.stream().map(each -> FileImportOccurrenceInfo.of(each, thesaurus)).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", data, queryParameters);
    }

    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public PagedInfoList geAllImportOccurrencesOccurrences(@BeanParam JsonQueryParameters queryParameters,
                                                                           @BeanParam JsonQueryFilter filter,
                                                                           @QueryParam("application") String applicationName,
                                                                           @Context SecurityContext securityContext) {

        List<FileImportOccurrence> fileImportOccurences = getFileImportOccurrences(queryParameters, filter, applicationName, null);
        List<FileImportOccurrenceInfo> data = fileImportOccurences.stream().map(each -> FileImportOccurrenceInfo.of(each, thesaurus)).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", data, queryParameters);
    }

    private List<FileImportOccurrence> getFileImportOccurrences(JsonQueryParameters queryParameters, JsonQueryFilter filter, String applicationName, Long importServiceId) {
        FileImportOccurrenceFinderBuilder finderBuilder = fileImportService.getFileImportOccurrenceFinderBuilder(applicationName, importServiceId);

        if (filter.hasProperty("startedOnFrom")) {
            if(filter.hasProperty("startedOnTo"))
                finderBuilder.withStartDateIn(Range.closed(filter.getInstant("startedOnFrom"), filter.getInstant("startedOnTo")));
            else
                finderBuilder.withStartDateIn(Range.greaterThan(filter.getInstant("startedOnFrom")));
        } else if (filter.hasProperty("startedOnTo")) {
            finderBuilder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            if(filter.hasProperty("finishedOnTo"))
                finderBuilder.withEndDateIn(Range.closed(filter.getInstant("finishedOnFrom"),filter.getInstant("finishedOnTo")));
            else
                finderBuilder.withEndDateIn(Range.greaterThan(filter.getInstant("finishedOnFrom")));
        } else if (filter.hasProperty("finishedOnTo")) {
            finderBuilder.withEndDateIn(Range.closed(Instant.EPOCH, filter.getInstant("finishedOnTo")));
        }

        if (filter.hasProperty("importService")) {
            List<Long> importServices = filter.getLongList("importService");
            finderBuilder.withImportServiceIn(importServices);
        }
        if (filter.hasProperty("status")) {

            finderBuilder.withStatusIn(filter.getStringList("status")
                    .stream()
                    .map(Status::valueOf)
                    .collect(Collectors.toList()));
        }
        return finderBuilder.build().from(queryParameters).find();
    }



    @GET
    @Path("/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public FileImportOccurrenceInfo geFileImportOccurence(@BeanParam JsonQueryParameters queryParameters,
                                                           @BeanParam JsonQueryFilter filter,
                                                           @PathParam("occurrenceId") long occurrenceId,
                                                           @Context SecurityContext securityContext) {

        return FileImportOccurrenceInfo.of(
                fileImportService.getFileImportOccurrence(occurrenceId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)), thesaurus);
    }

    @GET
    @Path("/history/{occurrenceId}/logs")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES, Privileges.VIEW_MDC_IMPORT_SERVICES})
    public PagedInfoList geFileImportOccurrenceLogEntries(@BeanParam JsonQueryParameters queryParameters,
                                                    @BeanParam JsonQueryFilter filter,
                                                    @PathParam("occurrenceId") long occurrenceId,
                                                    @Context SecurityContext securityContext) {

        List<ImportLogEntry> logEntries = fileImportService
                .getFileImportOccurrence(occurrenceId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))
                .getLogsFinder().from(queryParameters).find();

        List<ImportLogEntryInfo> data = logEntries.stream().map(each -> new ImportLogEntryInfo(each, thesaurus)).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", data, queryParameters);
    }

    private void updateProperties(FileImportScheduleInfo info, ImportSchedule importSchedule) {
        List<PropertySpec> propertiesSpecs = fileImportService.getPropertiesSpecsForImporter(info.importerInfo.name);
        propertiesSpecs.stream()
                .forEach(spec -> {
                    Object value = propertyUtils.findPropertyValue(spec, info.properties);
                    importSchedule.setProperty(spec.getName(), value);
                });
    }


    private ImportSchedule fetchImportSchedule(long id) {
        return fileImportService.getImportSchedule(id)
                .filter(is -> !is.isDeleted())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private ImportSchedule fetchImportScheduleIncludeDeleted(long id) {
        return fileImportService.getImportSchedule(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }




}
