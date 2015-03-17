package com.elster.jupiter.validation.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Path("/datavalidationtasks")
public class DataValidationTaskResource {

    private final RestQueryService queryService;
    private final ValidationService validationService;
    private final MeteringGroupsService meteringGroupsService;
    private final TransactionService transactionService;


    @Inject
    public DataValidationTaskResource(RestQueryService queryService, ValidationService validationService, TransactionService transactionService, MeteringGroupsService meteringGroupsService) {
        this.queryService = queryService;
        this.validationService = validationService;
        this.transactionService = transactionService;
        this.meteringGroupsService = meteringGroupsService;
        //this.thesaurus = thesaurus;
        //this.timeService = timeService;
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
        return Response.status(Response.Status.CREATED).entity(new DataValidationTaskInfo(dataValidationTask)).build();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION,
            Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getDataValidationTasks(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<DataValidationTask> list = getValidationTaskRestQuery().select(queryParameters, Order.ascending("name").toLowerCase());
        return PagedInfoList.asJson("dataValidationTasks", list.stream().map(dataValidationTask ->
                new DataValidationTaskInfo(dataValidationTask)).collect(Collectors.toList())
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
        return new DataValidationTaskInfo(task);//, thesaurus, timeService);
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
            task.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DataValidationTaskInfo(task)).build();

    }

    @POST
    @Path("/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_VALIDATION_CONFIGURATION, Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response triggerDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        //transactionService.execute(VoidTransaction.of(() -> fetchDataValidationtTask(id).triggerNow()));
        return Response.status(Response.Status.OK).build();
    }

    private EndDeviceGroup endDeviceGroup(long endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(endDeviceGroupId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private ScheduleExpression getScheduleExpression(DataValidationTaskInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
    }

    private RestQuery<DataValidationTask> getValidationTaskRestQuery() {
        Query<DataValidationTask> query = validationService.findValidationTasksQuery();
        return queryService.wrap(query);
    }
}
