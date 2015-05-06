package com.elster.jupiter.validation.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.*;
import com.elster.jupiter.validation.security.Privileges;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Path("/validationtasks")
public class DataValidationTaskResource {

    private final RestQueryService queryService;
    private final ValidationService validationService;
    private final MeteringGroupsService meteringGroupsService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final TimeService timeService;

    @Inject
    public DataValidationTaskResource(RestQueryService queryService, ValidationService validationService, TransactionService transactionService,
                                      MeteringGroupsService meteringGroupsService, TimeService timeService, Thesaurus thesaurus) {
        this.queryService = queryService;
        this.validationService = validationService;
        this.transactionService = transactionService;
        this.meteringGroupsService = meteringGroupsService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response createDataValidationTask(DataValidationTaskInfo info) {

        DataValidationTaskBuilder builder = validationService.newTaskBuilder()
                .setName(info.name)
                .setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id))
                .setScheduleExpression(getScheduleExpression(info))
                .setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));

        DataValidationTask dataValidationTask = builder.build();

        try (TransactionContext context = transactionService.getContext()) {
            dataValidationTask.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DataValidationTaskInfo(dataValidationTask, thesaurus)).build();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION,
            Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getDataValidationTasks(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<DataValidationTask> list = getValidationTaskRestQuery().select(queryParameters, Order.ascending("name").toLowerCase());
        return PagedInfoList.asJson("dataValidationTasks", list.stream().map(dataValidationTask ->
                new DataValidationTaskInfo(dataValidationTask, thesaurus)).collect(Collectors.toList())
                , queryParameters);
    }

    @DELETE
    @Path("/{dataValidationTaskId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response deleteDataValidationTask(@PathParam("dataValidationTaskId") final long dataValidationTaskId, @Context final SecurityContext securityContext) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {

                validationService.findValidationTask(dataValidationTaskId).
                        orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)).
                        delete();
            }
        });
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{dataValidationTaskId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION})
    public DataValidationTaskInfo getDataValidationTask(@PathParam("dataValidationTaskId") long dataValidationTaskId, @Context SecurityContext securityContext) {
        DataValidationTask task = validationService.findValidationTask(dataValidationTaskId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return new DataValidationTaskInfo(task, thesaurus);//, thesaurus, timeService);
    }

    @PUT
    @Path("/{dataValidationTaskId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response updateReadingTypeDataExportTask(@PathParam("dataValidationTaskId") long dataValidationTaskId, DataValidationTaskInfo info) {
        DataValidationTask task = validationService.findValidationTask(dataValidationTaskId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        try (TransactionContext context = transactionService.getContext()) {
            task.setName(info.name);
            task.setScheduleExpression(getScheduleExpression(info));
            task.setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id));
            task.setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));
            task.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DataValidationTaskInfo(task, thesaurus)).build();

    }

    @POST
    @Path("/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_VALIDATION_CONFIGURATION, Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response triggerDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        //transactionService.execute(VoidTransaction.of(() -> fetchDataValidationtTask(id).triggerNow()));
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public DataValidationTaskHistoryInfos getDataExportTaskHistory(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                                   @BeanParam JsonQueryFilter filter, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        DataValidationTask task = fetchDataValidationTask(id);
        DataValidationOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder()
                .setStart(queryParameters.getStartInt())
                .setLimit(queryParameters.getLimit() + 1);

        if (filter.hasProperty("startedOnFrom")) {
            if(filter.hasProperty("startedOnTo"))
                occurrencesFinder.withStartDateIn(Range.closed(filter.getInstant("startedOnFrom"),filter.getInstant("startedOnTo")));
            else
                occurrencesFinder.withStartDateIn(Range.greaterThan(filter.getInstant("startedOnFrom")));
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            if(filter.hasProperty("finishedOnTo"))
                occurrencesFinder.withEndDateIn(Range.closed(filter.getInstant("finishedOnFrom"),filter.getInstant("finishedOnTo")));
            else
                occurrencesFinder.withEndDateIn(Range.greaterThan(filter.getInstant("finishedOnFrom")));
        } else if (filter.hasProperty("finishedOnTo")) {
            occurrencesFinder.withEndDateIn(Range.closed(Instant.EPOCH, filter.getInstant("finishedOnTo")));
        }

        List<? extends DataValidationOccurrence> occurrences = occurrencesFinder.find();

        DataValidationTaskHistoryInfos infos = new DataValidationTaskHistoryInfos(task, queryParameters.clipToLimit(occurrences), thesaurus, timeService);
        infos.total = queryParameters.determineTotal(occurrences.size());
        return infos;
    }

    @GET
    @Path("/{id}/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public DataValidationOccurrenceLogInfos getDataExportTaskHistory(@PathParam("id") long id, @PathParam("occurrenceId") long occurrenceId,
                                                                     @Context SecurityContext securityContext, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        DataValidationTask task = fetchDataValidationTask(id);
        DataValidationOccurrence occurrence = fetchDataValidationOccurrence(occurrenceId, task);
        LogEntryFinder finder = occurrence.getLogsFinder()
                .setStart(queryParameters.getStartInt())
                .setLimit(queryParameters.getLimit());

        List<? extends LogEntry> occurrences = finder.find();

        DataValidationOccurrenceLogInfos infos = new DataValidationOccurrenceLogInfos(queryParameters.clipToLimit(occurrences), thesaurus);
        infos.total = queryParameters.determineTotal(occurrences.size());
        return infos;
    }

    private DataValidationTask fetchDataValidationTask(long id) {
        return validationService.findValidationTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private DataValidationOccurrence fetchDataValidationOccurrence(long id, DataValidationTask task) {
        return task.getOccurrence(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private EndDeviceGroup endDeviceGroup(long endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(endDeviceGroupId).orElse(null);
    }

    private ScheduleExpression getScheduleExpression(DataValidationTaskInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
    }

    private RestQuery<DataValidationTask> getValidationTaskRestQuery() {
        Query<DataValidationTask> query = validationService.findValidationTasksQuery();
        return queryService.wrap(query);
    }
}
