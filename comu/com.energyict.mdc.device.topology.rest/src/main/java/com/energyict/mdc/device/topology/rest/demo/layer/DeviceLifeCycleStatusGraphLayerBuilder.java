package com.energyict.mdc.device.topology.rest.demo.layer;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.layer.LayerNames;


import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Having the devices within the same network topology in different life cycle states (in 'Operational Stage') (=Active, Inactive)
 * Copyrights EnergyICT
 * Date: 31/08/2017
 * Time: 15:08
 */
public class DeviceLifeCycleStatusGraphLayerBuilder implements GraphLayerBuilder{

    private final DeviceLifeCycleService deviceLifeCycleService;

    public DeviceLifeCycleStatusGraphLayerBuilder(DeviceLifeCycleService deviceLifeCycleService){
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    @Override
    public boolean isGraphLayerCompatible(GraphLayer layer) {
        return LayerNames.DeviceLifeCycleStatusLayer.fullName().equals(layer.getName());
    }

    @Override
    public void buildLayer(Device device) {
        // Execute a randomly chosen Executable Action so to have devices with different lifecycle state
        List<ExecutableAction> possibleActions = getExecutableActions(device).stream().filter(this::leadsToStateInOperationalStage).collect(Collectors.toList());
        if (!possibleActions.isEmpty()) {
            Collections.shuffle(possibleActions);
            this.execute(possibleActions.get(0), device, Clock.systemDefaultZone().instant());
        }
    }

    private List<ExecutableAction> getExecutableActions(Device device) {
        return deviceLifeCycleService.getExecutableActions(device);
    }

    private void execute(ExecutableAction action, Device device, Instant effectiveTimestamp) {
        AuthorizedTransitionAction authorizedTransitionAction = (AuthorizedTransitionAction) action.getAction();
        List<ExecutableActionProperty> properties =
                DecoratedStream
                    .decorate(authorizedTransitionAction.getActions().stream())
                    .flatMap(ma -> this.deviceLifeCycleService.getPropertySpecsFor(ma).stream())
                    .distinct(PropertySpec::getName)
                    .map(ps -> this.toExecutableActionProperty(ps, device, effectiveTimestamp))
                    .collect(Collectors.toList());
        action.execute(effectiveTimestamp, properties);
    }

    private boolean leadsToStateInOperationalStage(ExecutableAction executableAction){
        AuthorizedTransitionAction authorizedTransitionAction = (AuthorizedTransitionAction) executableAction.getAction();
        Optional<Stage> stage = authorizedTransitionAction.getStateTransition().getTo().getStage();
        return stage.isPresent() && stage.get().getName().equals(EndDeviceStage.OPERATIONAL.getKey());
    }

    private ExecutableActionProperty toExecutableActionProperty(PropertySpec propertySpec, Device device, Instant effectiveTimestamp) {
        try {
            if (DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key().equals(propertySpec.getName())) {
                return this.deviceLifeCycleService.toExecutableActionProperty(effectiveTimestamp, propertySpec);
            } else if (DeviceLifeCycleService.MicroActionPropertyName.MULTIPLIER.key().equals(propertySpec.getName())) {
                return this.deviceLifeCycleService.toExecutableActionProperty(device.getMultiplier(), propertySpec);
            }else {
                throw new IllegalArgumentException("Unknown or unsupported PropertySpec: " + propertySpec.getName() + " that requires value type: " + propertySpec.getValueFactory().getValueType());
            }
        } catch (InvalidValueException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

}
