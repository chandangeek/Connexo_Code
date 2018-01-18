/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.FileImportHistory;
import com.elster.jupiter.fileimport.FileImportHistoryBuilder;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportOccurrenceFinderBuilder;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportLogEntry;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.fileimport.Status;
import com.elster.jupiter.fileimport.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;

import com.google.common.collect.Range;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
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
    private final ThreadPrincipalService threadPrincipalService;
    private final Clock clock;
    private final AppService appService;
    private final ExceptionFactory exceptionFactory;
    private final JsonService jsonService;
    private final Thesaurus thesaurus;


    private static final int MAX_FILE_SIZE = 100 * 1024 * 1024;
    private static final int UNPROCESSIBLE_ENTITY = 422;

    @Inject
    public FileImportScheduleResource(FileImportService fileImportService, TransactionService transactionService, PropertyValueInfoService propertyValueInfoService, FileSystem fileSystem, FileImportScheduleInfoFactory fileImportScheduleInfoFactory, ConcurrentModificationExceptionFactory conflictFactory, Validator validator, ThreadPrincipalService threadPrincipalService, Clock clock, AppService appService, ExceptionFactory exceptionFactory, JsonService jsonService, Thesaurus thesaurus) {
        this.fileImportService = fileImportService;
        this.transactionService = transactionService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.fileSystem = fileSystem;
        this.fileImportScheduleInfoFactory = fileImportScheduleInfoFactory;
        this.conflictFactory = conflictFactory;
        this.validator = validator;
        this.threadPrincipalService = threadPrincipalService;
        this.clock = clock;
        this.appService = appService;
        this.exceptionFactory = exceptionFactory;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
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
    @Path("/fileupload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES, Privileges.Constants.IMPORT_FILE})
    @Transactional
    public Response uploadFile(@FormDataParam("file")InputStream inputStream,
                               @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
                               @FormDataParam("scheduleId") InputStream scheduleId) throws IOException {
        long importScheduleId = parseScheduleId(scheduleId);
        ImportSchedule importSchedule = fileImportService.getImportSchedule(importScheduleId)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.IMPORT_SERVICE_NOT_FOUND, importScheduleId));

        AppServer appServer = findAppServerWithImportSchedule(importSchedule);

        String importFolder = String.valueOf(appServer.getImportDirectory().get().toAbsolutePath()
                .resolve(importSchedule.getImportDirectory()));
        String fileName = contentDispositionHeader.getFileName();

        if (fileName == null || fileName.isEmpty()) {
            throw new WebApplicationException(Response.status(UNPROCESSIBLE_ENTITY).entity(jsonService
                    .serialize(new ConstraintViolationInfo(thesaurus).from(new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "file")))).build());
        }
        loadFile(inputStream, fileName, importFolder, appServer.getName());
        FileImportHistory importHistory = buildFileImportHistory(importSchedule, fileName);
        return Response.status(Response.Status.CREATED).entity(FileImportHistoryInfo.from(importHistory)).build();
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
                .setActiveInUI(info.activeInUI)
                .setScheduleExpression(ScanFrequency.toScheduleExpression(info.scanFrequency));

        List<PropertySpec> propertiesSpecs = fileImportService.getPropertiesSpecsForImporter(info.importerInfo.name);

        propertiesSpecs
                .forEach(spec -> {
                    Object value = propertyValueInfoService.findPropertyValue(spec, info.properties);
                    if (value != null) {
                        builder.addProperty(spec.getName()).withValue(value);
                    }
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
            importSchedule.setActiveInUI(info.activeInUI);
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
        List<FileImportOccurrence> fileImportOccurrences = getFileImportOccurrences(queryParameters, filter, applicationName, importServiceId);
        List<FileImportOccurrenceInfo> data = fileImportOccurrences.stream().map(FileImportOccurrenceInfo::of).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", data, queryParameters);
    }

    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES, Privileges.Constants.VIEW_HISTORY})
    public PagedInfoList geAllImportOccurrencesOccurrences(@BeanParam JsonQueryParameters queryParameters,
                                                           @BeanParam JsonQueryFilter filter,
                                                           @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                           @Context SecurityContext securityContext) {
        List<FileImportOccurrence> fileImportOccurrences = getFileImportOccurrences(queryParameters, filter, applicationName, null);
        List<FileImportOccurrenceInfo> data = fileImportOccurrences.stream().map(FileImportOccurrenceInfo::of).collect(Collectors.toList());
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES, Privileges.Constants.VIEW_HISTORY})
    public FileImportOccurrenceInfo geFileImportOccurrence(@BeanParam JsonQueryParameters queryParameters,
                                                           @BeanParam JsonQueryFilter filter,
                                                           @PathParam("occurrenceId") long occurrenceId,
                                                           @Context SecurityContext securityContext) {

        return FileImportOccurrenceInfo.of(
                fileImportService.getFileImportOccurrence(occurrenceId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)));
    }

    @GET
    @Path("/history/{occurrenceId}/logs")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES, Privileges.Constants.VIEW_HISTORY})
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

    private AppServer findAppServerWithImportSchedule(ImportSchedule importSchedule) {
        return appService.findAppServers()
                .stream()
                .filter(AppServer::isActive)
                .filter(server -> server.getImportSchedulesOnAppServer()
                        .stream()
                        .map(schedule -> schedule.getImportSchedule().orElse(null))
                        .anyMatch(importSchedule::equals))
                .sorted(Comparator.comparing(AppServer::getName))
                .findFirst()
                .orElse(null);
    }

    private void loadFile(InputStream inputStream, String fileName, String importFolder, String appServerName) {
        File copiedFile = new File(importFolder + File.separator + FilenameUtils.getBaseName(fileName) + ".tmp");
        try(FileOutputStream outputStream = new FileOutputStream(copiedFile.getPath()); InputStream ins = inputStream ) {
            byte[] buffer = new byte[1024];
            int length;
            while((length = ins.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
                if (outputStream.getChannel().size() > MAX_FILE_SIZE) {
                    outputStream.close();
                    FileUtils.deleteQuietly(copiedFile);
                    throw new WebApplicationException(Response.status(UNPROCESSIBLE_ENTITY).entity(jsonService
                            .serialize(new ConstraintViolationInfo(thesaurus).from(new LocalizedFieldValidationException(MessageSeeds.MAX_FILE_SIZE_EXCEEDED, "file")))).build());
                }
            }
        } catch (IOException ex) {
            throw new WebApplicationException(Response.status(UNPROCESSIBLE_ENTITY).entity(jsonService
                    .serialize(new ConstraintViolationInfo(thesaurus).from(exceptionFactory.newException(MessageSeeds.FAILED_TO_UPLOAD_TO_SERVER, fileName, appServerName)))).build());
        }
        copiedFile.renameTo(new File(importFolder + File.separator + fileName));
    }

    private long parseScheduleId(InputStream inputStream) {
        Scanner sc = new Scanner(inputStream);
        if(sc.hasNextLong()) {
            return sc.nextLong();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private FileImportHistory buildFileImportHistory(ImportSchedule importSchedule, String fileName) {
        FileImportHistoryBuilder fileImportHistoryBuilder = fileImportService.newFileImportHistoryBuilder();
        fileImportHistoryBuilder.setImportSchedule(importSchedule);
        fileImportHistoryBuilder.setUserName(threadPrincipalService.getPrincipal().getName());
        fileImportHistoryBuilder.setFileName(fileName);
        fileImportHistoryBuilder.setUploadTime(clock.instant());
        return fileImportHistoryBuilder.create();
    }

}