package com.elster.jupiter.demo.impl.builders.device;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 24/09/2015
 * Time: 14:49
 */
public class SetDeviceInActiveLifeCycleStatePostBuilder implements Consumer<Device> {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleConfigurationService DLCconfigurationService;
    private final DeviceLifeCycleService DLCService;
    private final Clock clock;

    private Optional<DeviceLifeCycle> defaultLifeCycle;
    private List<AuthorizedTransitionAction> authorizedActions;
    private DeviceType deviceType;

    @Inject
    public SetDeviceInActiveLifeCycleStatePostBuilder(DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService DLCconfigurationService, DeviceLifeCycleService DLCService,
                                                      Clock clock) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.DLCconfigurationService = DLCconfigurationService;
        this.DLCService = DLCService;
        this.clock = clock;
    }

    @Override
    public void accept(Device device) {
        initDefaultLifeCycle();
        if (defaultLifeCycle.isPresent()) {
            initAuthorizedActionsToExecute();
        }
        if (this.deviceType == null || device.getDeviceType().getId() != this.deviceType.getId()) {
            this.deviceType = device.getDeviceType();
            if (this.deviceType.getDeviceLifeCycle() == null) {
                deviceConfigurationService.changeDeviceLifeCycle(deviceType, defaultLifeCycle.get());
            }
        }
        if (!authorizedActions.isEmpty()) {
            Long now = Clock.systemDefaultZone().millis();
            AuthorizedTransitionAction authorizedActionToExecute = authorizedActions.get(0);
            List<ExecutableActionProperty> properties =
                    DecoratedStream
                            .decorate(authorizedActionToExecute.getActions().stream())
                            .flatMap(ma -> this.DLCService.getPropertySpecsFor(ma).stream())
                            .distinct(PropertySpec::getName)
                            .map(ps -> this.toExecutableActionProperty(ps, clock.instant().plus(5, ChronoUnit.MINUTES)))
                            .collect(Collectors.toList());

            System.out.println(" ==> Finding the executable action propertiessetting took " + (Clock.systemDefaultZone().millis() - now) + " ms.");
            executeAuthorizedAction(authorizedActionToExecute, device, properties);
        }
    }

    private void initDefaultLifeCycle() {
        defaultLifeCycle = DLCconfigurationService.findDefaultDeviceLifeCycle();
    }

    private void initAuthorizedActionsToExecute() {
        authorizedActions = defaultLifeCycle.get().getAuthorizedActions(defaultLifeCycle.get().getFiniteStateMachine().getInitialState())
                .stream()
                .filter(action -> action instanceof AuthorizedTransitionAction)
                .map(action -> (AuthorizedTransitionAction) action)
                .filter(action -> action.getStateTransition().getTo().getName().equals(DefaultState.ACTIVE.getKey()))
                .collect(Collectors.toList());
    }


    private ExecutableActionProperty toExecutableActionProperty(PropertySpec propertySpec, Instant effectiveTimestamp) {
        try {
            if (DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key().equals(propertySpec.getName())) {
                return this.DLCService.toExecutableActionProperty(effectiveTimestamp, propertySpec);
            } else {
                throw new IllegalArgumentException("Unknown or unsupported PropertySpec: " + propertySpec.getName() + " that requires value type: " + propertySpec.getValueFactory().getValueType());
            }
        } catch (InvalidValueException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    private void executeAuthorizedAction(AuthorizedTransitionAction authorizedActionToExecute, Device device, List<ExecutableActionProperty> properties) {
        long now = Clock.systemDefaultZone().millis();
        try {
            DLCService.execute(authorizedActionToExecute, device, clock.instant().plus(5, ChronoUnit.MINUTES), properties);
            System.out.println(" ==> Setting the 'Active' State for device " + device.getmRID() + " took " + (Clock.systemDefaultZone().millis() - now) + " ms.");
        } catch (DeviceLifeCycleActionViolationException e) {
            e.printStackTrace();
        }
    }
}
