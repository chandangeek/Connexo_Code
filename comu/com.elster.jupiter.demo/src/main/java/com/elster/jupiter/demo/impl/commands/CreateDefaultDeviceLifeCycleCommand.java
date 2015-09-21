package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.*;
import com.energyict.mdc.device.lifecycle.config.*;

import javax.inject.Inject;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Purpose for this command is to install the Default life cycle in the demo system.
 * All devicetypes should have this lifecycle and all devices should be in the 'Active' State.
 *
 * Copyrights EnergyICT
 * Date: 8/09/2015
 * Time: 15:16
 */
public class CreateDefaultDeviceLifeCycleCommand {

    private final DeviceLifeCycleConfigurationService DLCconfigurationService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final DeviceService deviceService;
    private final Clock clock;

    private DeviceLifeCycle defaultLifeCycle;
    private Instant lastCheckedDate;

    @Inject
    public CreateDefaultDeviceLifeCycleCommand(DeviceLifeCycleConfigurationService DLCconfigurationService,
                                               DeviceConfigurationService deviceConfigurationService,
                                               DeviceLifeCycleService deviceLifeCycleService,
                                               FiniteStateMachineService finiteStateMachineService,
                                               DeviceService deviceService,
                                               Clock clock) {
        this.DLCconfigurationService = DLCconfigurationService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.deviceService = deviceService;
        this.clock = clock;
    }

    public void setLastCheckedDate(String lastCheckedDate) {
        this.lastCheckedDate = Instant.parse(lastCheckedDate + "T00:00:00Z");
    }

    public void run() {
        if (this.lastCheckedDate == null){
            throw new UnableToCreate("Please specify at which date the validation of devices need to start");
        }
        // Make sure the default device life cycle exists
        defaultLifeCycle = DLCconfigurationService.findDefaultDeviceLifeCycle().orElseGet(()->DLCconfigurationService.newDefaultDeviceLifeCycle("dlc.standard.device.life.cycle"));
        // Use this one as device life cycle for each existing device type
        deviceConfigurationService.findAllDeviceTypes().stream().forEach(deviceType -> deviceConfigurationService.changeDeviceLifeCycle(deviceType,defaultLifeCycle));
        // Set all devices to the 'Active' state
        deviceService.deviceQuery().select(Condition.TRUE, Order.NOORDER)
                .stream()
                .forEach(x -> this.setDeviceToState(x, DefaultCustomStateTransitionEventType.ACTIVATED));
    }

    private void setDeviceToState(Device device, DefaultCustomStateTransitionEventType eventType) {
        Optional<ExecutableAction> action =  deviceLifeCycleService.getExecutableActions(device, eventType.findOrCreate(finiteStateMachineService));
        if (action.isPresent() && action.get().getAction() instanceof AuthorizedTransitionAction){
            AuthorizedTransitionAction authorizedTransitionAction = (AuthorizedTransitionAction) action.get().getAction();
            List<ExecutableActionProperty> properties =
                    DecoratedStream
                        .decorate(authorizedTransitionAction.getActions().stream())
                        .flatMap(ma -> this.deviceLifeCycleService.getPropertySpecsFor(ma).stream())
                        .distinct(PropertySpec::getName)
                        .map(ps -> this.toExecutableActionProperty(ps, clock.instant()))
                        .collect(Collectors.toList());
            try {
                action.get().execute(clock.instant(), properties);
                System.out.println(" ==> device state for device " + device.getName() + "(MRID: "+ device.getmRID()+"): "+ device.getState().getName());
            }catch(Exception exception){
                System.err.println("!!! ==> Activating device " + device.getName() + "(MRID: "+ device.getmRID()+") failed");
                exception.printStackTrace();
            }
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
