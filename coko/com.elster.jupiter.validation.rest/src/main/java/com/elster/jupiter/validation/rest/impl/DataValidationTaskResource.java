package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.KorePagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationOccurrenceFinder;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationOccurrenceLogInfos;
import com.elster.jupiter.validation.rest.DataValidationTaskHistoryInfos;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;
import com.elster.jupiter.validation.security.Privileges;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
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
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
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
        return Response.status(Response.Status.CREATED).entity(new DataValidationTaskInfo(dataValidationTask, thesaurus, timeService)).build();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public KorePagedInfoList getDataValidationTasks(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<DataValidationTask> list = getValidationTaskRestQuery().select(queryParameters, Order.ascending("name").toLowerCase());
        return KorePagedInfoList.asJson("dataValidationTasks", list.stream().map(dataValidationTask ->
                new DataValidationTaskInfo(dataValidationTask, thesaurus, timeService)).collect(Collectors.toList())
                , queryParameters);
    }

    @DELETE
    @Path("/{dataValidationTaskId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public DataValidationTaskInfo getDataValidationTask(@PathParam("dataValidationTaskId") long dataValidationTaskId, @Context SecurityContext securityContext) {
        DataValidationTask task = validationService.findValidationTask(dataValidationTaskId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return new DataValidationTaskInfo(task, thesaurus, timeService);
    }

    @PUT
    @Path("/{dataValidationTaskId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response updateReadingTypeDataValidationTask(@PathParam("dataValidationTaskId") long dataValidationTaskId, DataValidationTaskInfo info) {
        DataValidationTask task = validationService.findValidationTask(dataValidationTaskId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        try (TransactionContext context = transactionService.getContext()) {
            task.setName(info.name);
            task.setScheduleExpression(getScheduleExpression(info));
            task.setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id));
            task.setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));
            task.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DataValidationTaskInfo(task, thesaurus, timeService)).build();

    }

    @POST
    @Path("/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response triggerDataValidationTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        transactionService.execute(VoidTransaction.of(() -> fetchDataValidationTask(id).triggerNow()));
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public DataValidationTaskHistoryInfos getDataValidationTaskHistory(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                                   @BeanParam JsonQueryFilter filter, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        DataValidationTask task = fetchDataValidationTask(id);
        DataValidationOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder()
                .setStart(queryParameters.getStartInt())
                .setLimit(queryParameters.getLimit() + 1);

        if (filter.hasProperty("startedOnFrom")) {
            if(filter.hasProperty("startedOnTo")) {
                occurrencesFinder.withStartDateIn(filter.getClosedRange("startedOnFrom","startedOnTo"));
            }
            else {
                occurrencesFinder.withStartDateIn(Range.greaterThan(filter.getInstant("startedOnFrom")));
            }
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            if(filter.hasProperty("finishedOnTo")) {
                occurrencesFinder.withEndDateIn(filter.getClosedRange("finishedOnFrom", "finishedOnTo"));
            } else {
                occurrencesFinder.withEndDateIn(Range.greaterThan(filter.getInstant("finishedOnFrom")));
            }
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
    public DataValidationOccurrenceLogInfos getDataValidationTaskHistory(@PathParam("id") long id, @PathParam("occurrenceId") long occurrenceId,
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
