/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportOccurrenceFinderBuilder;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportLogEntry;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.fileimport.Status;
import com.elster.jupiter.fileimport.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/importservices")
public class FileImportScheduleResource {

    private final FileImportService fileImportService;
    private final TransactionService transactionService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final FileSystem fileSystem;
    private final FileImportScheduleInfoFactory fileImportScheduleInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final Validator validator;

    @Inject
    public FileImportScheduleResource(FileImportService fileImportService, TransactionService transactionService, PropertyValueInfoService propertyValueInfoService, FileSystem fileSystem, FileImportScheduleInfoFactory fileImportScheduleInfoFactory, ConcurrentModificationExceptionFactory conflictFactory, Validator validator) {
        this.fileImportService = fileImportService;
        this.transactionService = transactionService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.fileSystem = fileSystem;
        this.fileImportScheduleInfoFactory = fileImportScheduleInfoFactory;
        this.conflictFactory = conflictFactory;
        this.validator = validator;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES})
    public PagedInfoList getImportSchedules(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName) {
        List<FileImportScheduleInfo> importScheduleInfos = fileImportService.findImportSchedules(applicationName).from(queryParameters).stream()
                .map(fileImportScheduleInfoFactory::asInfo).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("importSchedules", importScheduleInfos, queryParameters);
    }

    @GET
    @Path("/list/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES})
    public PagedInfoList getImportSchedulesList(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName) {
        List<ImportSchedule> list = fileImportService.findAllImportSchedules(applicationName).from(queryParameters).find();
        List<ImportServiceNameInfo> data = list.stream().map(ImportServiceNameInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("importSchedules", data, queryParameters);
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES})
    public FileImportScheduleInfo getImportSchedule(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return fileImportScheduleInfoFactory.asInfo(fetchImportSchedule(id));
    }

    @GET
    @Path("/list/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES})
    public FileImportScheduleInfo getImportScheduleIncludeDeleted(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return fileImportScheduleInfoFactory.asInfo(fetchImportScheduleIncludeDeleted(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES})
    @Transactional
    public Response addImportSchedule(FileImportScheduleInfo info) {
        if (info.scanFrequency < 0) {
            info.scanFrequency = 1;
        }
        validate(info, POST.class);
        ImportScheduleBuilder builder = fileImportService.newBuilder()
                .setName(info.name)
                .setPathMatcher(info.pathMatcher)
                .setImportDirectory(getPath(info.importDirectory))
                .setFailureDirectory(getPath(info.failureDirectory))
                .setSuccessDirectory(getPath(info.successDirectory))
                .setProcessingDirectory(getPath(info.inProcessDirectory))
                .setImporterName(info.importerInfo.name)
                .setScheduleExpression(ScanFrequency.toScheduleExpression(info.scanFrequency));

        List<PropertySpec> propertiesSpecs = fileImportService.getPropertiesSpecsForImporter(info.importerInfo.name);

        propertiesSpecs
                .forEach(spec -> {
                    Object value = propertyValueInfoService.findPropertyValue(spec, info.properties);
                    builder.addProperty(spec.getName()).withValue(value);
                });
        ImportSchedule importSchedule;
        importSchedule = builder.create();

        return Response.status(Response.Status.CREATED).entity(fileImportScheduleInfoFactory.asInfo(importSchedule)).build();
    }

    private void validate(Object info, Class<?> clazz) {
        Set<ConstraintViolation<Object>> violations = validator.validate(info, clazz);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    @DELETE
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES})
    public Response removeImportSchedule(@PathParam("id") long id, FileImportScheduleInfo info) {
        info.id = id;
        String scheduleName = info.name;
        try (TransactionContext context = transactionService.getContext()) {
            ImportSchedule importSchedule = fetchAndLockImportSchedule(info);
            if (!importSchedule.isDeleted()) {
                importSchedule.delete();
                scheduleName = importSchedule.getName();
                context.commit();
            }

            return Response.status(Response.Status.OK).build();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.DELETE_IMPORT_SCHEDULE_SQL_EXCEPTION, "status", scheduleName);
        }
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES})
    public Response updateImportSchedule(@PathParam("id") long id, FileImportScheduleInfo info) {
        if (info.scanFrequency < 0) {
            info.scanFrequency = 1;
        }
        validate(info, PUT.class);
        try (TransactionContext context = transactionService.getContext()) {
            ImportSchedule importSchedule = fetchAndLockImportSchedule(info);
            if (!importSchedule.isImporterAvailable()) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            importSchedule.setName(info.name);
            importSchedule.setActive(info.active);
            importSchedule.setImportDirectory(getPath(info.importDirectory));
            importSchedule.setFailureDirectory(getPath(info.failureDirectory));
            importSchedule.setSuccessDirectory(getPath(info.successDirectory));
            importSchedule.setProcessingDirectory(getPath(info.inProcessDirectory));
            importSchedule.setImporterName(info.importerInfo.name);
            importSchedule.setPathMatcher(info.pathMatcher);
            importSchedule.setScheduleExpression(ScanFrequency.toScheduleExpression(info.scanFrequency));
            updateProperties(info, importSchedule);

            importSchedule.update();
            context.commit();
            return Response.status(Response.Status.OK).entity(fileImportScheduleInfoFactory.asInfo(importSchedule)).build();
        }
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES})
    public PagedInfoList getImportScheduleOccurrences(@BeanParam JsonQueryParameters queryParameters,
                                                      @BeanParam JsonQueryFilter filter,
                                                      @PathParam("id") long importServiceId,
                                                      @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                      @Context SecurityContext securityContext) {
        List<FileImportOccurrence> fileImportOccurences = getFileImportOccurrences(queryParameters, filter, applicationName, importServiceId);
        List<FileImportOccurrenceInfo> data = fileImportOccurences.stream().map(FileImportOccurrenceInfo::of).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", data, queryParameters);
    }

    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES})
    public PagedInfoList geAllImportOccurrencesOccurrences(@BeanParam JsonQueryParameters queryParameters,
                                                           @BeanParam JsonQueryFilter filter,
                                                           @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                           @Context SecurityContext securityContext) {
        List<FileImportOccurrence> fileImportOccurences = getFileImportOccurrences(queryParameters, filter, applicationName, null);
        List<FileImportOccurrenceInfo> data = fileImportOccurences.stream().map(FileImportOccurrenceInfo::of).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", data, queryParameters);
    }

    private List<FileImportOccurrence> getFileImportOccurrences(JsonQueryParameters queryParameters, JsonQueryFilter filter, String applicationName, Long importServiceId) {
        FileImportOccurrenceFinderBuilder finderBuilder = fileImportService.getFileImportOccurrenceFinderBuilder(applicationName, importServiceId);

        if (filter.hasProperty("startedOnFrom")) {
            if (filter.hasProperty("startedOnTo")) {
                finderBuilder.withStartDateIn(Range.closed(filter.getInstant("startedOnFrom"), filter.getInstant("startedOnTo")));
            } else {
                finderBuilder.withStartDateIn(Range.greaterThan(filter.getInstant("startedOnFrom")));
            }
        } else if (filter.hasProperty("startedOnTo")) {
            finderBuilder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            if (filter.hasProperty("finishedOnTo")) {
                finderBuilder.withEndDateIn(Range.closed(filter.getInstant("finishedOnFrom"), filter.getInstant("finishedOnTo")));
            } else {
                finderBuilder.withEndDateIn(Range.greaterThan(filter.getInstant("finishedOnFrom")));
            }
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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES})
    public FileImportOccurrenceInfo geFileImportOccurence(@BeanParam JsonQueryParameters queryParameters,
                                                          @BeanParam JsonQueryFilter filter,
                                                          @PathParam("occurrenceId") long occurrenceId,
                                                          @Context SecurityContext securityContext) {

        return FileImportOccurrenceInfo.of(
                fileImportService.getFileImportOccurrence(occurrenceId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)));
    }

    @GET
    @Path("/history/{occurrenceId}/logs")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES})
    public PagedInfoList geFileImportOccurrenceLogEntries(@BeanParam JsonQueryParameters queryParameters,
                                                          @BeanParam JsonQueryFilter filter,
                                                          @PathParam("occurrenceId") long occurrenceId,
                                                          @Context SecurityContext securityContext) {

        List<ImportLogEntry> logEntries = fileImportService
                .getFileImportOccurrence(occurrenceId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))
                .getLogsFinder().from(queryParameters).find();

        List<ImportLogEntryInfo> data = logEntries.stream().map(ImportLogEntryInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", data, queryParameters);
    }

    private void updateProperties(FileImportScheduleInfo info, ImportSchedule importSchedule) {
        List<PropertySpec> propertiesSpecs = fileImportService.getPropertiesSpecsForImporter(info.importerInfo.name);
        propertiesSpecs
                .forEach(spec -> {
                    Object value = propertyValueInfoService.findPropertyValue(spec, info.properties);
                    importSchedule.setProperty(spec.getName(), value);
                });
    }

    private ImportSchedule fetchAndLockImportSchedule(FileImportScheduleInfo info){
        return fileImportService.findAndLockImportScheduleByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> fileImportService.getImportSchedule(info.id).map(ImportSchedule::getVersion).orElse(null))
                        .supplier());
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

    private java.nio.file.Path getPath(String value){
        try {
            return fileSystem.getPath(value);
        } catch (InvalidPathException e ){
            throw new IllegalArgumentException(e);
        }
    }

}