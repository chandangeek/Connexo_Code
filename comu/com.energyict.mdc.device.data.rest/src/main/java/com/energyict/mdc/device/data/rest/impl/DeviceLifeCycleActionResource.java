package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.ActionDoesNotRelateToDeviceStateException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.RequiredMicroActionPropertiesException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MicroCheckTranslationKey;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DeviceLifeCycleActionResource {

    private final DeviceLifeCycleService deviceLifeCycleService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final DeviceLifeCycleActionInfoFactory deviceLifeCycleActionInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceLifeCycleActionResource(
            DeviceLifeCycleService deviceLifeCycleService,
            ResourceHelper resourceHelper,
            ExceptionFactory exceptionFactory,
            DeviceLifeCycleActionInfoFactory deviceLifeCycleActionInfoFactory,
            Thesaurus thesaurus) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.deviceLifeCycleActionInfoFactory = deviceLifeCycleActionInfoFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE})
    public Response getAvailableActionsForCurrentDevice(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<IdWithNameInfo> availableActions = deviceLifeCycleService.getExecutableActions(device)
                .stream()
                .map(executableAction -> new IdWithNameInfo(executableAction.getAction()))
                .sorted((a1, a2) -> a1.name.compareToIgnoreCase(a2.name))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("transitions", availableActions, queryParameters)).build();
    }

    @GET
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE})
    public Response getPropertiesForAction(@PathParam("mRID") String mrid, @PathParam("actionId") long actionId, @BeanParam JsonQueryParameters queryParameters){
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ExecutableAction requestedAction = getExecuteActionByIdOrThrowException(actionId, device);
        DeviceLifeCycleActionInfo info = deviceLifeCycleActionInfoFactory.from(requestedAction);
        return Response.ok(info).build();
    }

    @PUT
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE})
    public Response executeAction(
            @PathParam("mRID") String mrid,
            @PathParam("actionId") long actionId,
            @BeanParam JsonQueryParameters queryParameters,
            DeviceLifeCycleActionInfo info){
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        resourceHelper.findDeviceAndLock(device.getId(), info.deviceVersion);
        ExecutableAction requestedAction = getExecuteActionByIdOrThrowException(actionId, device);
        DeviceLifeCycleActionResultInfo wizardResult = new DeviceLifeCycleActionResultInfo();
        if (requestedAction.getAction() instanceof AuthorizedTransitionAction){
            AuthorizedTransitionAction authorizedAction = (AuthorizedTransitionAction) requestedAction.getAction();
            wizardResult.targetState = getTargetStateName(authorizedAction);
            if (info.properties != null){
                Map<String, PropertySpec> allPropertySpecsForAction = authorizedAction.getActions()
                        .stream()
                        .flatMap(microAction -> deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                        .collect(Collectors.toMap(propertySpec -> propertySpec.getName(), Function.<PropertySpec>identity(), (prop1, prop2) -> prop1));
                List<ExecutableActionProperty> executableProperties = getExecutableActionPropertiesFromInfo(info, allPropertySpecsForAction);
                try {
                    requestedAction.execute(executableProperties);
                } catch (SecurityException ex){
                    wizardResult.result = false;
                    wizardResult.message = ex.getLocalizedMessage();
                } catch (ActionDoesNotRelateToDeviceStateException violationEx){
                    wizardResult.result = false;
                    wizardResult.message = violationEx.getLocalizedMessage();
                } catch (RequiredMicroActionPropertiesException violationEx){
                    wrapWithFormValidationErrorAndRethrow(violationEx);
                } catch (MultipleMicroCheckViolationsException violationEx){
                    getFailedExecutionMessage(violationEx, wizardResult);
                }
            }
        }
        return Response.ok(wizardResult).build();
    }

    private void wrapWithFormValidationErrorAndRethrow(RequiredMicroActionPropertiesException violationEx) {
        RestValidationBuilder formValidationErrorBuilder = new RestValidationBuilder();
        violationEx.getViolatedPropertySpecNames()
                .stream()
                .forEach( propertyName ->
                    formValidationErrorBuilder.addValidationError(
                            new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, propertyName)));
        formValidationErrorBuilder.validate();
    }

    private String getTargetStateName(AuthorizedTransitionAction requestedAction) {
        State targetState  = requestedAction.getStateTransition().getTo();
        String targetStateName = targetState.getName();
        Optional<DefaultState> defaultState = DefaultState.from(targetState);
        if (defaultState.isPresent()){
            targetStateName = thesaurus.getString(defaultState.get().getKey(), defaultState.get().getKey()) ;
        }
        return targetStateName;
    }

    private void getFailedExecutionMessage(MultipleMicroCheckViolationsException microChecksViolationEx, DeviceLifeCycleActionResultInfo wizardResult) {
        wizardResult.result = false;
        wizardResult.message = DefaultTranslationKey.PRE_TRANSITION_CHECKS_FAILED.translateWith(thesaurus);
        wizardResult.microChecks = microChecksViolationEx.getViolations()
                .stream()
                .map(violation -> {
                    IdWithNameInfo microCheckInfo = new IdWithNameInfo();
                    microCheckInfo.id = violation.getCheck().name();
                    MicroCheckTranslationKey.getNameTranslation(violation.getCheck()).ifPresent(microCheckName ->
                                    microCheckInfo.id = thesaurus.getString(microCheckName.getKey(), microCheckName.getDefaultFormat())
                    );
                    microCheckInfo.name = violation.getCheck().name();
                    MicroCheckTranslationKey.getDescriptionTranslation(violation.getCheck()).ifPresent(microCheckDescription ->
                                    microCheckInfo.name = thesaurus.getString(microCheckDescription.getKey(), microCheckDescription.getDefaultFormat())
                    );
                    return microCheckInfo;
                })
                .collect(Collectors.toList());
    }

    private List<ExecutableActionProperty> getExecutableActionPropertiesFromInfo(DeviceLifeCycleActionInfo info, Map<String, PropertySpec> allPropertySpecsForAction) {
        List<ExecutableActionProperty> executableProperties = new ArrayList<>(allPropertySpecsForAction.size());
        for (PropertyInfo property : info.properties) {
            PropertySpec propertySpec = allPropertySpecsForAction.get(property.key);
            if (propertySpec != null && property.propertyValueInfo != null){
                try {
                    Object value = null;
                    if (property.propertyValueInfo.value != null) {
                        value = propertySpec.getValueFactory().fromStringValue(String.valueOf(property.propertyValueInfo.value));
                    }
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

    private ExecutableAction getExecuteActionByIdOrThrowException(@PathParam("actionId") long actionId, Device device) {
        return deviceLifeCycleService.getExecutableActions(device)
                .stream()
                .filter(candidate -> candidate.getAction().getId() == actionId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_LIFE_CYCLE_ACTION, actionId));
    }
}
