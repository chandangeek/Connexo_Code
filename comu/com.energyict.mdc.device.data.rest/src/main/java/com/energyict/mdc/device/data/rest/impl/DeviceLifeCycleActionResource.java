/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.InvalidLastCheckedException;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.RequiredMicroActionPropertiesException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DeviceLifeCycleActionResource {

    private final DeviceLifeCycleService deviceLifeCycleService;
    private final TransactionService transactionService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final DeviceLifeCycleActionInfoFactory deviceLifeCycleActionInfoFactory;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final Clock clock;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceLifeCycleActionResource(
            DeviceLifeCycleService deviceLifeCycleService,
            TransactionService transactionService, ResourceHelper resourceHelper,
            ExceptionFactory exceptionFactory,
            DeviceLifeCycleActionInfoFactory deviceLifeCycleActionInfoFactory,
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            Clock clock,
            Thesaurus thesaurus) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.transactionService = transactionService;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.deviceLifeCycleActionInfoFactory = deviceLifeCycleActionInfoFactory;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getAvailableActionsForCurrentDevice(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<IdWithNameInfo> availableActions = deviceLifeCycleService.getExecutableActions(device)
                .stream()
                .map(executableAction -> new IdWithNameInfo(executableAction.getAction()))
                .sorted((a1, a2) -> a1.name.compareToIgnoreCase(a2.name))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("transitions", availableActions, queryParameters)).build();
    }

    @GET @Transactional
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getPropertiesForAction(@PathParam("name") String name, @PathParam("actionId") long actionId, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        ExecutableAction requestedAction = getExecuteActionByIdOrThrowException(actionId, device);
        DeviceLifeCycleActionInfo info = deviceLifeCycleActionInfoFactory.from(requestedAction);
        return Response.ok(info).build();
    }

    @PUT
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response executeAction(
            @PathParam("name") String name,
            @PathParam("actionId") long actionId,
            @BeanParam JsonQueryParameters queryParameters,
            DeviceLifeCycleActionInfo info){
        try (TransactionContext transaction = transactionService.getContext()) {
            Device device = resourceHelper.lockDeviceOrThrowException(info.device);
            ExecutableAction requestedAction = getExecuteActionByIdOrThrowException(actionId, device);
            DeviceLifeCycleActionResultInfo wizardResult = new DeviceLifeCycleActionResultInfo();
            info.effectiveTimestamp = info.effectiveTimestamp == null ? clock.instant() : info.effectiveTimestamp;
            wizardResult.effectiveTimestamp = info.effectiveTimestamp;
            if (requestedAction.getAction() instanceof AuthorizedTransitionAction) {
                AuthorizedTransitionAction authorizedAction = (AuthorizedTransitionAction) requestedAction.getAction();
                wizardResult.targetState = getTargetStateName(authorizedAction);
                if (info.properties != null) {
                    Map<String, PropertySpec> allPropertySpecsForAction = authorizedAction.getActions()
                            .stream()
                            .flatMap(microAction -> deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                            .collect(Collectors.toMap(PropertySpec::getName, Function.<PropertySpec>identity(), (prop1, prop2) -> prop1));
                    List<ExecutableActionProperty> executableProperties = getExecutableActionPropertiesFromInfo(info, allPropertySpecsForAction);
                    try {
                        requestedAction.execute(info.effectiveTimestamp, executableProperties);
                        transaction.commit();
                    } catch (RequiredMicroActionPropertiesException violationEx) {
                        wrapWithFormValidationErrorAndRethrow(violationEx);
                    } catch (MultipleMicroCheckViolationsException violationEx) {
                        getFailedExecutionMessage(violationEx, wizardResult);
                    } catch (SecurityException | InvalidLastCheckedException | DeviceLifeCycleActionViolationException ex) {
                        wizardResult.result = false;
                        wizardResult.message = ex.getLocalizedMessage();
                    }
                }
            }
            return Response.ok(wizardResult).build();
        }
    }

    private void wrapWithFormValidationErrorAndRethrow(RequiredMicroActionPropertiesException violationEx) {
        RestValidationBuilder formValidationErrorBuilder = new RestValidationBuilder();
        violationEx.getViolatedPropertySpecNames()
                .forEach( propertyName ->
                    formValidationErrorBuilder.addValidationError(
                            new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, propertyName)));
        formValidationErrorBuilder.validate();
    }

    private String getTargetStateName(AuthorizedTransitionAction requestedAction) {
        State targetState  = requestedAction.getStateTransition().getTo();
        return DefaultState
                .from(targetState)
                .map(deviceLifeCycleConfigurationService::getDisplayName)
                .orElseGet(targetState::getName);
    }

    private void getFailedExecutionMessage(MultipleMicroCheckViolationsException microChecksViolationEx, DeviceLifeCycleActionResultInfo wizardResult) {
        wizardResult.result = false;
        wizardResult.message = DefaultTranslationKey.PRE_TRANSITION_CHECKS_FAILED.translateWith(thesaurus);
        wizardResult.microChecks = DecoratedStream.decorate(microChecksViolationEx.getViolations().stream())
                .map(violation -> {
                    IdWithNameInfo microCheckInfo = new IdWithNameInfo();
                    MicroCheck microCheck = violation.getCheck();
                    microCheckInfo.id = deviceLifeCycleService.getName(microCheck);
                    microCheckInfo.name = deviceLifeCycleService.getDescription(microCheck);
                    return microCheckInfo;
                })
                .distinct(check -> check.id)
                .collect(Collectors.toList());
    }

    private List<ExecutableActionProperty> getExecutableActionPropertiesFromInfo(DeviceLifeCycleActionInfo info, Map<String, PropertySpec> allPropertySpecsForAction) {
        List<ExecutableActionProperty> executableProperties = new ArrayList<>(allPropertySpecsForAction.size());
        for (PropertyInfo property : info.properties) {
            PropertySpec propertySpec = allPropertySpecsForAction.get(property.key);
            if (propertySpec != null && property.propertyValueInfo != null
                    && property.propertyValueInfo.value != null && !"".equals(property.propertyValueInfo.value)) {
                try {
                    Object value = propertySpec.getValueFactory().fromStringValue(String.valueOf(property.propertyValueInfo.value));
                    executableProperties.add(deviceLifeCycleService.toExecutableActionProperty(value, propertySpec));
                } catch (InvalidValueException e) {
                    // Enable form validation
                    String propertyName = propertySpec.getName();
                    if (e.getArguments() != null && e.getArguments().length > 0){
                        propertyName = (String) e.getArguments()[0]; // property name from exception
                    }
                    throw new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, propertyName);
                }
            }
        }
        return executableProperties;
    }

    private ExecutableAction getExecuteActionByIdOrThrowException(long actionId, Device device) {
        return deviceLifeCycleService.getExecutableActions(device)
                .stream()
                .filter(candidate -> candidate.getAction().getId() == actionId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_LIFE_CYCLE_ACTION, actionId));
    }
}
