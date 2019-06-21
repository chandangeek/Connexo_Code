package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
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
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Path("/crlprops")
public class CrlRequestTaskResource {
    private final TaskService taskService;
    private final MessageService messageService;
    private final ExceptionFactory exceptionFactory;
    private final CrlRequestTaskInfoFactory crlRequestTaskInfoFactory;
    private final SecurityManagementService securityManagementService;
    private final CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    private final Thesaurus thesaurus;
    private static final String CRL_REQUEST_TASK_TYPE = "CRL request";

    @Inject
    public CrlRequestTaskResource(TaskService taskService,
                                  MessageService messageService,
                                  ExceptionFactory exceptionFactory,
                                  CrlRequestTaskInfoFactory crlRequestTaskInfoFactory,
                                  SecurityManagementService securityManagementService,
                                  CrlRequestTaskPropertiesService crlRequestTaskPropertiesService,
                                  Thesaurus thesaurus
    ) {
        this.taskService = taskService;
        this.messageService = messageService;
        this.exceptionFactory = exceptionFactory;
        this.crlRequestTaskInfoFactory = crlRequestTaskInfoFactory;
        this.securityManagementService = securityManagementService;
        this.crlRequestTaskPropertiesService = crlRequestTaskPropertiesService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public PagedInfoList gelAllCrlRequestTaskProperties(@BeanParam JsonQueryParameters queryParameters) {
        List<CrlRequestTaskPropertyInfo> infoList = crlRequestTaskPropertiesService.findCrlRequestTaskProperties()
                .stream()
                .map(crlRequestTaskInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("crlRequestTaskProperties", infoList, queryParameters);
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response gelCrlRequestTaskProperty(@PathParam("id") Long id, @BeanParam JsonQueryParameters queryParameters) {
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).orElseThrow(
                () -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CRL_REQUEST_TASK));
        CrlRequestTaskProperty crlRequestTaskProperty = crlRequestTaskPropertiesService.getCrlRequestTaskPropertiesForCa(recurrentTask).orElseThrow(
                () -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CRL_REQUEST_TASK_PROPERTIES));
        return Response.ok(crlRequestTaskInfoFactory.asInfo(crlRequestTaskProperty)).build();
    }

    @GET
    @Transactional
    @Path("/securityaccessors")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public PagedInfoList getSecurityAccessors(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList("securityAccessors", getSecurityAccessors(), queryParameters);
    }

    @GET
    @Transactional
    @Path("/loglevels")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public PagedInfoList getLogLevels(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList("logLevels", getLogLevels(), queryParameters);
    }

    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response deleteCrlRequestTaskCA(@PathParam("id") Long id) {
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).orElseThrow(
                () -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CRL_REQUEST_TASK));
        CrlRequestTaskProperty crlRequestTaskProperty = crlRequestTaskPropertiesService.getCrlRequestTaskPropertiesForCa(recurrentTask).orElseThrow(
                () -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CRL_REQUEST_TASK_PROPERTIES));
        crlRequestTaskPropertiesService.deleteCrlRequestTaskPropertiesForCa(recurrentTask);
        recurrentTask.delete();
        return Response.noContent().build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response createCrlRequestTaskCA(CrlRequestTaskPropertyInfo info) {
        Optional<RecurrentTask> recurrentTask = createCrlRequestRecurrentTask(info);
        if (recurrentTask.isPresent()) {
            createCrlRequestTaskProperties(info, recurrentTask.get());
            info.task = new IdWithNameInfo(recurrentTask.get().getId(), recurrentTask.get().getName());
            return Response.status(Response.Status.CREATED).entity(info).build();
        }
        throw new LocalizedFieldValidationException(MessageSeeds.CRL_REQUEST_TASK_CA_NAME_UNIQUE, "caName");
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response updateCrlRequestTaskCA(@PathParam("id") Long id, CrlRequestTaskPropertyInfo info) {
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).orElseThrow(
                () -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CRL_REQUEST_TASK));
        CrlRequestTaskProperty crlRequestTaskProperty = crlRequestTaskPropertiesService.getCrlRequestTaskPropertiesForCa(recurrentTask).orElseThrow(
                () -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CRL_REQUEST_TASK_PROPERTIES));
        Optional<RecurrentTask> updatedTask = updateCrlRequestRecurrentTask(info, recurrentTask);
        if (updatedTask.isPresent()) {
            updateCrlRequestTaskProperties(info, updatedTask.get());
            info.task = new IdWithNameInfo(updatedTask.get().getId(), updatedTask.get().getName());
            return Response.status(Response.Status.OK).entity(info).build();
        }
        throw new LocalizedFieldValidationException(MessageSeeds.CRL_REQUEST_TASK_CA_NAME_UNIQUE, "caName");
    }

    @PUT
    @Transactional
    @Path("/run/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response runCrlRequestTaskCA(@PathParam("id") Long id) {
        RecurrentTask recurrentTask = taskService.getRecurrentTask(id).orElseThrow(
                () -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CRL_REQUEST_TASK));
        recurrentTask.triggerNow();
        return Response.ok().build();
    }

    private DestinationSpec getCrlRequestDestination() {
        return messageService.getDestinationSpec(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME).orElseGet(() ->
                messageService.getQueueTableSpec(MessageService.PRIORITIZED_ROW_QUEUE_TABLE)
                        .get()
                        .createDestinationSpec(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME, CrlRequestHandlerFactory.DEFAULT_RETRY_DELAY_IN_SECONDS, false, true));
    }

    private Optional<RecurrentTask> createCrlRequestRecurrentTask(CrlRequestTaskPropertyInfo info) {
        RecurrentTask task;
        String taskName = getTaskName(info);
        if (!taskService.getRecurrentTask(taskName).isPresent()) {
            task = taskService.newBuilder()
                    .setApplication("MultiSense")
                    .setName(taskName)
                    .setScheduleExpression(getScheduleExpression(info))
                    .setDestination(getCrlRequestDestination())
                    .setPayLoad("Crl Request")
                    .scheduleImmediately(true)
                    .setFirstExecution(info.nextRun)
                    .build();
            task.setLogLevel((Integer) info.logLevel.id);
            task.save();
        } else {
            return Optional.empty();
        }
        return Optional.of(task);
    }

    private void createCrlRequestTaskProperties(CrlRequestTaskPropertyInfo info, RecurrentTask recurrentTask) {
        SecurityAccessor securityAccessor = securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .stream()
                .filter(sa -> sa.getKeyAccessorType().getId() == (Integer) info.securityAccessor.id)
                .filter(sa -> sa.getActualValue().isPresent() &&
                        sa.getActualValue().get() instanceof CertificateWrapper &&
                        ((CertificateWrapper) sa.getActualValue().get()).getCertificate().isPresent())
                .findAny()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KEY_ACCESSOR, info.securityAccessor.id));
        String caName = info.caName;
        if (!crlRequestTaskPropertiesService.getCrlRequestTaskPropertiesForCa(recurrentTask).isPresent()) {
            crlRequestTaskPropertiesService.createCrlRequestTaskPropertiesForCa(recurrentTask, securityAccessor, caName);
        } else {
            crlRequestTaskPropertiesService.updateCrlRequestTaskPropertiesForCa(recurrentTask, securityAccessor, caName);
        }
    }

    private Optional<RecurrentTask> updateCrlRequestRecurrentTask(CrlRequestTaskPropertyInfo info, RecurrentTask recurrentTask) {
        String taskName = getTaskName(info);
        if (taskService.getRecurrentTask(taskName).isPresent()) {
            if (taskService.getRecurrentTask(taskName).get().getId() != recurrentTask.getId()) {
                return Optional.empty();
            }
        }
        recurrentTask.setScheduleExpression(getScheduleExpression(info));
        recurrentTask.setNextExecution(info.nextRun);
        recurrentTask.setLogLevel((Integer) info.logLevel.id);
        recurrentTask.setName(taskName);
        recurrentTask.save();
        recurrentTask.updateNextExecution();
        return Optional.of(recurrentTask);
    }

    private void updateCrlRequestTaskProperties(CrlRequestTaskPropertyInfo info, RecurrentTask recurrentTask) {
        SecurityAccessor securityAccessor = securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .stream()
                .filter(sa -> sa.getKeyAccessorType().getId() == (Integer) info.securityAccessor.id)
                .filter(sa -> sa.getActualValue().isPresent() &&
                        sa.getActualValue().get() instanceof CertificateWrapper &&
                        ((CertificateWrapper) sa.getActualValue().get()).getCertificate().isPresent())
                .findAny()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KEY_ACCESSOR, info.securityAccessor.id));
        String caName = info.caName;
        if (crlRequestTaskPropertiesService.getCrlRequestTaskPropertiesForCa(recurrentTask).isPresent()) {
            crlRequestTaskPropertiesService.updateCrlRequestTaskPropertiesForCa(recurrentTask, securityAccessor, caName);
        } else {
            crlRequestTaskPropertiesService.createCrlRequestTaskPropertiesForCa(recurrentTask, securityAccessor, caName);
        }
    }

    private ScheduleExpression getScheduleExpression(CrlRequestTaskPropertyInfo info) {
        return info.periodicalExpressionInfo == null ? Never.NEVER : info.periodicalExpressionInfo.toExpression();
    }

    private List<IdWithNameInfo> getLogLevels() {
        List<IdWithNameInfo> logLevels = new ArrayList<>();
        logLevels.add(new IdWithNameInfo(Level.SEVERE.intValue(), "Error"));
        logLevels.add(new IdWithNameInfo(Level.WARNING.intValue(), "Warning"));
        logLevels.add(new IdWithNameInfo(Level.INFO.intValue(), "Information"));
        logLevels.add(new IdWithNameInfo(Level.FINE.intValue(), "Debug"));
        logLevels.add(new IdWithNameInfo(Level.FINEST.intValue(), "Trace"));
        return logLevels;
    }

    private List<IdWithNameInfo> getSecurityAccessors() {
        return securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .stream()
                .filter(securityAccessor -> securityAccessor.getActualValue().isPresent() &&
                        securityAccessor.getActualValue().get() instanceof CertificateWrapper &&
                        ((CertificateWrapper) securityAccessor.getActualValue().get()).getCertificate().isPresent())
                .map(SecurityAccessor::getKeyAccessorType)
                .map(securityAccessorType -> new IdWithNameInfo(securityAccessorType.getId(), securityAccessorType.getName()))
                .collect(Collectors.toList());
    }

    private String getTaskName(CrlRequestTaskPropertyInfo info) {
        return info.caName + " - " + CRL_REQUEST_TASK_TYPE;
    }

}
