package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Purpose for this command is to install the Default life cycle in the demo system.
 * All devicetypes should have this lifecycle and all devices should be in the 'Active' State.
 * <p>
 * Copyrights EnergyICT
 * Date: 8/09/2015
 * Time: 15:16
 */
public class CreateDefaultDeviceLifeCycleCommand {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final DeviceService deviceService;
    private final Clock clock;

    private DeviceLifeCycle defaultLifeCycle;
    private Instant lastCheckedDate;

    @Inject
    public CreateDefaultDeviceLifeCycleCommand(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                               DeviceConfigurationService deviceConfigurationService,
                                               DeviceLifeCycleService deviceLifeCycleService,
                                               DeviceService deviceService,
                                               Clock clock) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.deviceService = deviceService;
        this.clock = clock;
    }

    public void setLastCheckedDate(String lastCheckedDate) {
        this.lastCheckedDate = Instant.parse(lastCheckedDate + "T00:00:00Z");
    }

    public void run() {
        if (this.lastCheckedDate == null) {
            throw new UnableToCreate("Please specify at which date the validation of devices need to start");
        }
        // Make sure the default device life cycle exists
        defaultLifeCycle = deviceLifeCycleConfigurationService.findDefaultDeviceLifeCycle()
                .orElseGet(() -> deviceLifeCycleConfigurationService.newDefaultDeviceLifeCycle("dlc.standard.device.life.cycle"));
        // Use this one as device life cycle for each existing device type
        // And set all devices of this type to the 'Active State'
        deviceConfigurationService.findAllDeviceTypes()
                .stream()
                .forEach(type -> changeDeviceLifeCycle(type, deviceService.findAllDevices(Where.where("deviceType").isEqualTo(type)).paged(0, 1000).find()));
    }

    private void changeDeviceLifeCycle(DeviceType deviceType, List<Device> devices) {
        long now = Clock.systemDefaultZone().millis();
        System.out.println(" ==> changing device life cycle for deviceType " + deviceType.getName() + " and activating all devices of this type");

        deviceConfigurationService.changeDeviceLifeCycle(deviceType, defaultLifeCycle);
        System.out.println(" ==> changing device life cycle for deviceType " + deviceType.getName() + " took " + (Clock.systemDefaultZone().millis() - now) + " ms.");
        List<AuthorizedTransitionAction> authorizedActions =
                defaultLifeCycle.getAuthorizedActions(defaultLifeCycle.getFiniteStateMachine().getInitialState()).stream()
                        .filter(action -> action instanceof AuthorizedTransitionAction)
                        .map(action -> (AuthorizedTransitionAction) action)
                        .filter(action -> action.getStateTransition().getTo().getName().equals(DefaultState.ACTIVE.getKey()))
                        .collect(Collectors.toList());
        if (!authorizedActions.isEmpty()) {
            AuthorizedTransitionAction authorizedActionToExecute = authorizedActions.get(0);
            List<ExecutableActionProperty> properties =
                    DecoratedStream
                            .decorate(authorizedActionToExecute.getActions().stream())
                            .flatMap(ma -> this.deviceLifeCycleService.getPropertySpecsFor(ma).stream())
                            .distinct(PropertySpec::getName)
                            .map(ps -> this.toExecutableActionProperty(ps, clock.instant()))
                            .collect(Collectors.toList());
            devices.forEach(x -> executeAuthorizedAction(authorizedActionToExecute, x, properties));
        }
    }

    private void executeAuthorizedAction(AuthorizedTransitionAction authorizedActionToExecute, Device device, List<ExecutableActionProperty> properties) {
        try {
            deviceLifeCycleService.execute(authorizedActionToExecute, device, clock.instant(), properties);
        } catch (DeviceLifeCycleActionViolationException e) {
            e.printStackTrace();
        }
    }

    private ExecutableActionProperty toExecutableActionProperty(PropertySpec propertySpec, Instant effectiveTimestamp) {
        try {
            if (DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key().equals(propertySpec.getName())) {
                return this.deviceLifeCycleService.toExecutableActionProperty(effectiveTimestamp, propertySpec);
            } else {
                throw new IllegalArgumentException("Unknown or unsupported PropertySpec: " + propertySpec.getName() + " that requires value type: " + propertySpec.getValueFactory().getValueType());
            }
        } catch (InvalidValueException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

}
