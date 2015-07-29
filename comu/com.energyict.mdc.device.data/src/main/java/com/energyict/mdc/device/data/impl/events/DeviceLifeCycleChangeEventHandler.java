package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.config.IncompatibleDeviceLifeCycleChangeException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.IncompatibleFiniteStateMachineChangeException;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.conditions.Subquery;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Responds to {@link DeviceLifeCycleChangeEvent}s and will
 * check that there are no devices that are currently using
 * a {@link State} that no longer exists in the new
 * {@link com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-18 (11:49)
 */
@Component(name = "com.energyict.mdc.device.data.dlc.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceLifeCycleChangeEventHandler implements TopicHandler {

    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceDataModelService deviceDataModelService;
    private volatile MeteringService meteringService;

    // For OSGi framework
    public DeviceLifeCycleChangeEventHandler() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceLifeCycleChangeEventHandler(DeviceConfigurationService deviceConfigurationService, DeviceDataModelService deviceDataModelService, MeteringService meteringService) {
        this();
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setDeviceDataModelService(deviceDataModelService);
        this.setMeteringService(meteringService);
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Reference
    public void setMeteringService(MeteringService eventService) {
        this.meteringService = eventService;
    }

    @Override
    public String getTopicMatcher() {
        return this.deviceConfigurationService.changeDeviceLifeCycleTopicName();
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.handle((DeviceLifeCycleChangeEvent) localEvent.getSource());
    }

    private void handle(DeviceLifeCycleChangeEvent event) {
        Subquery matchDeviceType = this.deviceDataModelService
                .dataModel()
                .query(Device.class)
                .asSubquery(
                    where(DeviceFields.DEVICETYPE.name()).isEqualTo(event.getDeviceType().getId()),
                    "id");  // Selects only the id field
        try {
            this.meteringService.changeStateMachine(
                    event.getTimestamp(),
                    event.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine(),
                    event.getDeviceLifeCycle().getFiniteStateMachine(),
                    matchDeviceType);
        }
        catch (IncompatibleFiniteStateMachineChangeException e) {
            throw IncompatibleDeviceLifeCycleChangeException.wrapping(e);
        }
    }

}