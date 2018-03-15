package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;
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
import javax.ws.rs.POST;
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
    private final CrlRequestTaskService crlRequestTaskService;
    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;

    @Inject
    public CrlRequestTaskResource(TaskService taskService,
                                  MessageService messageService,
                                  ExceptionFactory exceptionFactory,
                                  CrlRequestTaskInfoFactory crlRequestTaskInfoFactory,
                                  SecurityManagementService securityManagementService,
                                  CrlRequestTaskService crlRequestTaskService,
                                  SecurityAccessorInfoFactory securityAccessorInfoFactory
    ) {
        this.taskService = taskService;
        this.messageService = messageService;
        this.exceptionFactory = exceptionFactory;
        this.crlRequestTaskInfoFactory = crlRequestTaskInfoFactory;
        this.securityManagementService = securityManagementService;
        this.crlRequestTaskService = crlRequestTaskService;
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response gelCrlRequestTaskProperties(@BeanParam JsonQueryParameters queryParameters) {
        Optional<CrlRequestTaskProperty> crlRequestTaskProperty = crlRequestTaskService.findCrlRequestTaskProperties();
        CrlRequestTaskPropertyInfo info = crlRequestTaskProperty.isPresent() ?
                crlRequestTaskInfoFactory.asInfo(crlRequestTaskProperty.get()) : new CrlRequestTaskPropertyInfo();
        return Response.ok(info).build();
    }

    @GET
    @Transactional
    @Path("/securityaccessors")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST,
            com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_1, com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_2, com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_3, com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_4,
            com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_1, com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_2, com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_3, com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_4})
    public Response gelSecurityAccessors(@BeanParam JsonQueryParameters queryParameters) {
        List<String> securityAccessorNamesList = securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .stream()
                .filter(securityAccessor -> securityAccessor.getActualValue().isPresent() &&
                        securityAccessor.getActualValue().get() instanceof CertificateWrapper &&
                        ((CertificateWrapper) securityAccessor.getActualValue().get()).getCertificate().isPresent())
                .map(securityAccessor -> securityAccessor.getKeyAccessorType().getName())
                .collect(Collectors.toList());
        CrlRequestTaskPropertyInfo info = new CrlRequestTaskPropertyInfo();
        info.securityAccessorNames = securityAccessorNamesList;
        return Response.ok(info).build();
    }

    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/delete")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response deleteCrlRequestTaskProperties(CrlRequestTaskPropertyInfo info) {
        deleteCrlPropsAndTask(info);
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/add")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST,
            com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_1, com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_2, com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_3, com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_4,
            com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_1, com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_2, com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_3, com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_4})
    public Response createCrlRequestTaskProperties(CrlRequestTaskPropertyInfo info) {
        deleteCrlPropsAndTask(info);
        RecurrentTask recurrentTask = createCrlRequestRecurrentTask(info);
        SecurityAccessor securityAccessor = securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .stream()
                .filter(sa -> sa.getKeyAccessorType().getName().equalsIgnoreCase(info.securityAccessorName))
                .filter(sa -> sa.getActualValue().isPresent() &&
                        sa.getActualValue().get() instanceof CertificateWrapper &&
                        ((CertificateWrapper) sa.getActualValue().get()).getCertificate().isPresent())
                .findAny()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KEY_ACCESSOR, info.securityAccessorName));
        String caName = info.caName;
        CrlRequestTaskProperty crlRequestTaskProperty = crlRequestTaskService.newCrlRequestTaskProperties();
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
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST,
            com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_1, com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_2, com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_3, com.elster.jupiter.pki.security.Privileges.Constants.VIEW_SECURITY_PROPERTIES_4,
            com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_1, com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_2, com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_3, com.elster.jupiter.pki.security.Privileges.Constants.EDIT_SECURITY_PROPERTIES_4})
    public Response updateCrlRequestTaskProperty(CrlRequestTaskPropertyInfo info) {
        deleteCrlPropsAndTask(info);
        RecurrentTask recurrentTask = createCrlRequestRecurrentTask(info);
        SecurityAccessor securityAccessor = securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .stream()
                .filter(sa -> sa.getKeyAccessorType().getName().equalsIgnoreCase(info.securityAccessorName))
                .filter(sa -> sa.getActualValue().isPresent() &&
                        sa.getActualValue().get() instanceof CertificateWrapper &&
                        ((CertificateWrapper) sa.getActualValue().get()).getCertificate().isPresent())
                .findAny()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KEY_ACCESSOR, info.securityAccessorName));
        String caName = info.caName;
        CrlRequestTaskProperty crlRequestTaskProperty = crlRequestTaskService.newCrlRequestTaskProperties();
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
        Optional<RecurrentTask> currentRecurrentTask = taskService.getRecurrentTask(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME);
        currentRecurrentTask.ifPresent(RecurrentTask::delete);
        RecurrentTask recurrentTask = taskService.newBuilder()
                .setApplication("MultiSense")
                .setName(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME)
                .setScheduleExpression(getScheduleExpression(info))
                .setDestination(getCrlRequestDestination())
                .setPayLoad("Crl Request")
                .scheduleImmediately(true)
                .build();
        recurrentTask.setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));
        return recurrentTask;
    }

    private TemporalExpression getScheduleExpression(CrlRequestTaskPropertyInfo info) {
        TimeDuration timeDuration = info.timeDurationInfo.asTimeDuration();
        return new TemporalExpression(timeDuration);
    }


    private void deleteCrlPropsAndTask(CrlRequestTaskPropertyInfo info) {
        Optional<CrlRequestTaskProperty> crlRequestTaskProperty = crlRequestTaskService.findCrlRequestTaskProperties();
        crlRequestTaskProperty.ifPresent(CrlRequestTaskProperty::delete);
        Optional<RecurrentTask> recurrentTask = taskService.getRecurrentTask(info.recurrentTaskName);
        recurrentTask.ifPresent(RecurrentTask::delete);
    }


}
