package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.CrlRequestService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.crlrequest.rest.CrlRequestTaskPropertyInfo;
import com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory;
import com.energyict.mdc.device.data.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/crlprops")
public class CrlRequestTaskResource {
    private final TaskService taskService;
    private final MessageService messageService;
    private final ExceptionFactory exceptionFactory;
    private final CrlRequestTaskInfoFactory crlRequestTaskInfoFactory;
    private final SecurityManagementService securityManagementService;
    private final CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;
    private final CrlRequestService crlRequestService;

    @Inject
    public CrlRequestTaskResource(TaskService taskService,
                                  MessageService messageService,
                                  ExceptionFactory exceptionFactory,
                                  CrlRequestTaskInfoFactory crlRequestTaskInfoFactory,
                                  SecurityManagementService securityManagementService,
                                  CrlRequestTaskPropertiesService crlRequestTaskPropertiesService,
                                  SecurityAccessorInfoFactory securityAccessorInfoFactory,
                                  CrlRequestService crlRequestService
    ) {
        this.taskService = taskService;
        this.messageService = messageService;
        this.exceptionFactory = exceptionFactory;
        this.crlRequestTaskInfoFactory = crlRequestTaskInfoFactory;
        this.securityManagementService = securityManagementService;
        this.crlRequestTaskPropertiesService = crlRequestTaskPropertiesService;
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
        this.crlRequestService = crlRequestService;
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response gelCrlRequestTaskProperties(@BeanParam JsonQueryParameters queryParameters) {
        Optional<CrlRequestTaskProperty> crlRequestTaskProperty = crlRequestTaskPropertiesService.findCrlRequestTaskProperties();
        CrlRequestTaskPropertyInfo info = crlRequestTaskProperty.isPresent() ?
                crlRequestTaskInfoFactory.asInfo(crlRequestTaskProperty.get()) : new CrlRequestTaskPropertyInfo();
        return Response.ok(info).build();
    }

    @GET
    @Transactional
    @Path("/securityaccessors")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public PagedInfoList getSecurityAccessors(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> securityAccessors = securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .stream()
                .filter(securityAccessor -> securityAccessor.getActualValue().isPresent() &&
                        securityAccessor.getActualValue().get() instanceof CertificateWrapper &&
                        ((CertificateWrapper) securityAccessor.getActualValue().get()).getCertificate().isPresent())
                .map(SecurityAccessor::getKeyAccessorType)
                .map(securityAccessorType -> new IdWithNameInfo(securityAccessorType.getId(), securityAccessorType.getName()))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("securityAccessors", securityAccessors, queryParameters);
    }

    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response deleteCrlRequestTaskProperties(CrlRequestTaskPropertyInfo info) {
        crlRequestTaskPropertiesService.deleteCrlRequestTaskProperties();
        taskService.getRecurrentTask(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME).ifPresent(RecurrentTask::suspend);
        return Response.noContent().build();
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response updateCrlRequestTaskProperty(CrlRequestTaskPropertyInfo info) {
        RecurrentTask recurrentTask = createOrUpdateCrlRequestRecurrentTask(info);
        createOrUpdateCrlRequestTaskProperties(info, recurrentTask);
        return Response.status(Response.Status.CREATED).entity(info).build();
    }

    @PUT
    @Transactional
    @Path("/run")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response runCrlRequestTask(@BeanParam JsonQueryParameters queryParameters) {
        crlRequestService.runNow();
        return Response.ok().build();
    }

    private DestinationSpec getCrlRequestDestination() {
        return messageService.getDestinationSpec(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME).orElseGet(() ->
                messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")
                        .get()
                        .createDestinationSpec(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME, CrlRequestHandlerFactory.DEFAULT_RETRY_DELAY_IN_SECONDS));
    }

    private RecurrentTask createOrUpdateCrlRequestRecurrentTask(CrlRequestTaskPropertyInfo info) {
        RecurrentTask task;
        if (!taskService.getRecurrentTask(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME).isPresent()) {
            task = taskService.newBuilder()
                    .setApplication("MultiSense")
                    .setName(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME)
                    .setScheduleExpression(getScheduleExpression(info))
                    .setDestination(getCrlRequestDestination())
                    .setPayLoad("Crl Request")
                    .scheduleImmediately(true)
                    .build();
            task.setNextExecution(info.nextRun == null ? null : info.nextRun);
            task.save();
        } else {
            task = taskService.getRecurrentTask(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME).get();
            task.setScheduleExpression(getScheduleExpression(info));
            task.setNextExecution(info.nextRun == null ? null : info.nextRun);
            task.save();
            task.updateNextExecution();
        }
        return task;
    }

    private void createOrUpdateCrlRequestTaskProperties(CrlRequestTaskPropertyInfo info, RecurrentTask recurrentTask) {
        SecurityAccessor securityAccessor = securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .stream()
                .filter(sa -> sa.getKeyAccessorType().getId() == (Integer) info.securityAccessor.id)
                .filter(sa -> sa.getActualValue().isPresent() &&
                        sa.getActualValue().get() instanceof CertificateWrapper &&
                        ((CertificateWrapper) sa.getActualValue().get()).getCertificate().isPresent())
                .findAny()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KEY_ACCESSOR, info.securityAccessor.id));

        if (crlRequestTaskPropertiesService.findCrlRequestTaskProperties().isPresent()) {
            crlRequestTaskPropertiesService.updateCrlRequestTaskProperties(recurrentTask, securityAccessor, info.caName);
        } else {
            crlRequestTaskPropertiesService.createCrlRequestTaskProperties(recurrentTask, securityAccessor, info.caName);
        }
    }

    private TemporalExpression getScheduleExpression(CrlRequestTaskPropertyInfo info) {
        TimeDuration timeDuration = info.timeDurationInfo.asTimeDuration();
        return new TemporalExpression(timeDuration);
    }

}
