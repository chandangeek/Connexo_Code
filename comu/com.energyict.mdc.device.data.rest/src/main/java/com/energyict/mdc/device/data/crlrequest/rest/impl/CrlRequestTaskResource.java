package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;
import com.energyict.mdc.device.data.crlrequest.rest.CrlRequestTaskPropertyInfo;
import com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Path("/crlrequesttaskprops")
public class CrlRequestTaskResource {
    private final TaskService taskService;
    private final MessageService messageService;
    private final ExceptionFactory exceptionFactory;
    private final CrlRequestTaskInfoFactory crlRequestTaskInfoFactory;
    private final SecurityManagementService securityManagementService;
    private final CrlRequestTaskService crlRequestTaskService;

    @Inject
    public CrlRequestTaskResource(TaskService taskService,
                                  MessageService messageService,
                                  ExceptionFactory exceptionFactory,
                                  CrlRequestTaskInfoFactory crlRequestTaskInfoFactory,
                                  SecurityManagementService securityManagementService,
                                  CrlRequestTaskService crlRequestTaskService) {
        this.taskService = taskService;
        this.messageService = messageService;
        this.exceptionFactory = exceptionFactory;
        this.crlRequestTaskInfoFactory = crlRequestTaskInfoFactory;
        this.securityManagementService = securityManagementService;
        this.crlRequestTaskService =crlRequestTaskService;
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public PagedInfoList gelCrlRequestTaskProperties(@BeanParam JsonQueryParameters queryParameters) {
        List<CrlRequestTaskPropertyInfo> infos = crlRequestTaskService.findAllCrlRequestTaskProperties()
                .stream()
                .map(crlRequestTaskInfoFactory::asInfo)
                .collect(toList());
        return PagedInfoList.fromPagedList("crlrequesttaskprops", infos, queryParameters);
    }

    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/delete")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response deleteCrlRequestTaskProperties(CrlRequestTaskPropertyInfo info) {
        crlRequestTaskService.findAllCrlRequestTaskProperties().forEach(CrlRequestTaskProperty::delete);
        Optional<RecurrentTask> recurrentTask = taskService.getRecurrentTask(info.recurrentTaskName);
        recurrentTask.ifPresent(RecurrentTask::delete);
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/add")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response createCrlRequestTaskProperty(CrlRequestTaskPropertyInfo info) {
        deleteCrlRequestTaskProperties(info);
        RecurrentTask recurrentTask = createCrlRequestRecurrentTask(info);
        SecurityAccessor securityAccessor = securityManagementService.findSecurityAccessorById(info.securityAccessorInfo.id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KEY_ACCESSOR, info.securityAccessorInfo.id));
        String caName = info.caName;
        CrlRequestTaskProperty crlRequestTaskProperty = crlRequestTaskService.newCrlRequestTaskProperty();
        crlRequestTaskProperty.setRecurrentTask(recurrentTask);
        crlRequestTaskProperty.setCaName(caName);
        crlRequestTaskProperty.setSecurityAccessor(securityAccessor);
        crlRequestTaskProperty.save();
        return Response.status(Response.Status.CREATED).entity(info).build();
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/update")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response updateCrlRequestTaskProperty(CrlRequestTaskPropertyInfo info) {
        deleteCrlRequestTaskProperties(info);
        RecurrentTask recurrentTask = createCrlRequestRecurrentTask(info);
        SecurityAccessor securityAccessor = securityManagementService.findSecurityAccessorById(info.securityAccessorInfo.id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KEY_ACCESSOR, info.securityAccessorInfo.id));
        String caName = info.caName;
        CrlRequestTaskProperty crlRequestTaskProperty = crlRequestTaskService.newCrlRequestTaskProperty();
        crlRequestTaskProperty.setRecurrentTask(recurrentTask);
        crlRequestTaskProperty.setCaName(caName);
        crlRequestTaskProperty.setSecurityAccessor(securityAccessor);
        crlRequestTaskProperty.save();
        return Response.status(Response.Status.CREATED).entity(info).build();
    }

    private DestinationSpec getCrlRequestDestination() {
        return messageService.getDestinationSpec(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME).orElseGet(() ->
                messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")
                        .get()
                        .createDestinationSpec(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME, CrlRequestHandlerFactory.DEFAULT_RETRY_DELAY_IN_SECONDS));
    }

    private RecurrentTask createCrlRequestRecurrentTask(CrlRequestTaskPropertyInfo info) {
        Optional<RecurrentTask> recurrentTask = taskService.getRecurrentTask(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME);
        recurrentTask.ifPresent(RecurrentTask::delete);
        return taskService.newBuilder()
                .setApplication("MultiSense")
                .setName(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME)
                .setScheduleExpression(getScheduleExpression(info))
                .setDestination(getCrlRequestDestination())
                .setPayLoad("Crl Request")
                .scheduleImmediately(true)
                .build();
    }

    private ScheduleExpression getScheduleExpression(CrlRequestTaskPropertyInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
    }

}
