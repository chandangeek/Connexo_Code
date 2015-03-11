package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.security.Privileges;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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

@Path("/datavalidationtasks")
public class DataValidationTaskResource {

    private final RestQueryService queryService;
    private final ValidationService validationService;
    //private final TimeService timeService;
    private final MeteringGroupsService meteringGroupsService;
    //private final Thesaurus thesaurus;
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

    /*
        @POST
        @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
        @Consumes(MediaType.APPLICATION_JSON)
        @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
        public Response createDataValidationTask(DataValidationTaskInfo info) {
            return Response.status(Response.Status.CREATED).entity(new DataValidationTaskInfo(transactionService.execute(new Transaction<DataValidationTask>() {
                @Override
                public DataValidationTask perform() {
                    return validationService.createValidationTask(info.name, info.endDeviceGroup);
                }
            }))).build();
        }
        */
        @POST
        @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
        @Consumes(MediaType.APPLICATION_JSON)
        @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
        public Response createDataValidationTask(DataValidationTaskInfo info) {

            DataValidationTaskBuilder builder = validationService.newTaskBuilder()
                    .setName(info.name)
                    .setEndDeviceGroup(endDeviceGroup(info.endDeviceGroup.id));
            /*
            .setDataProcessorName(info.dataProcessor.name)
                    .setScheduleExpression(getScheduleExpression(info))
                    .setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun))
                    .setExportPeriod(getRelativePeriod(info.exportperiod))
                    .setUpdatePeriod(getRelativePeriod(info.updatePeriod))
                    .setValidatedDataOption(info.validatedDataOption)
                    .setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id))
                    .exportContinuousData(info.exportContinuousData)
                    .exportUpdate(info.exportUpdate);

*/

            DataValidationTask dataValidationTask = builder.build();

            try (TransactionContext context = transactionService.getContext()) {
                //dataValidationTask.save();
                context.commit();
            }
            return Response.status(Response.Status.CREATED).entity(new DataValidationTaskInfo(dataValidationTask)).build();
            /*
            dataValidationTask
            return Response.status(Response.Status.CREATED).entity(dataValidationTask);
            return Response.status(Response.Status.CREATED).entity(new DataValidationTaskInfo(transactionService.execute(new Transaction<DataValidationTask>() {
                @Override
                public DataValidationTask perform() {

                    DataValidationTask dt =  validationService.createValidationTask(info.name);
                    dt.setEndDeviceGroup(endDeviceGroup(info.endDeviceGroup.id));

                    dt.save();
                    return dt;
                }
            }))).build();
            */
        }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION,
            Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public DataValidationTaskInfos getDataValidationTasks(@Context UriInfo uriInfo) {
        //QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());

        List<DataValidationTask> list = validationService.findValidationTasks();

        DataValidationTaskInfos infos = new DataValidationTaskInfos(list);//, this.thesaurus, this.timeService);

        return infos;


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
    public DataValidationTaskInfos editDataValidationTask(@PathParam("dataValidationTaskId") final long dataValidationTaskId, final DataValidationTaskInfo info, @Context SecurityContext securityContext) {
        //DataValidationTaskInfos result = new DataValidationTaskInfos();
        //result.add(transactionService.execute(new Transaction<DataValidationTask>() {
        /*    @Override

        public ValidationRule perform() {
                ValidationRuleSet ruleSet = validationService.getValidationRuleSet(ruleSetId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

                ValidationRule rule = getValidationRuleFromSetOrThrowException(ruleSet, ruleId);

                List<String> mRIDs = new ArrayList<>();
                for (ReadingTypeInfo readingTypeInfo : info.readingTypes) {
                    mRIDs.add(readingTypeInfo.mRID);
                }
                Map<String, Object> propertyMap = new HashMap<>();
                PropertyUtils propertyUtils = new PropertyUtils();
                for (PropertySpec propertySpec : rule.getPropertySpecs()) {
                    Object value = propertyUtils.findPropertyValue(propertySpec, info.properties);
                    if (value != null) {
                        propertyMap.put(propertySpec.getName(), value);
                    }
                }
                rule = ruleSet.updateRule(ruleId, info.name, info.active, mRIDs, propertyMap);
                ruleSet.save();
                return rule;
            }
        }));*/
        return null;
    }

    @POST
    @Path("/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_VALIDATION_CONFIGURATION, Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response triggerDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        //transactionService.execute(VoidTransaction.of(() -> fetchDataExportTask(id).triggerNow()));
        return Response.status(Response.Status.OK).build();
    }

    private EndDeviceGroup endDeviceGroup(long endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(endDeviceGroupId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

}
