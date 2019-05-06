/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name="com.energyict.mdc.device.topology.kpi.registered.devices.calculator.factory", service = MessageHandlerFactory.class, property = {"subscriber=" + RegisteredDevicesKpiCalculatorFactory.TASK_SUBSCRIBER, "destination=" + RegisteredDevicesKpiCalculatorFactory.TASK_DESTINATION}, immediate = true)
public class RegisteredDevicesKpiCalculatorFactory implements MessageHandlerFactory {

    public static final String TASK_DESTINATION = "MDCKpiRegisteredDevTopic";
    public static final String TASK_SUBSCRIBER = "MDCKpiRegisteredDevCalc";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Calculate registered devices kpi's";

    private volatile TaskService taskService;
    private volatile RegisteredDevicesKpiService registeredDevicesKpiService;
    private volatile TopologyService topologyService;
    private volatile EventService eventService;
    private volatile DeviceService deviceService;

    // For OSGi framework only
    public RegisteredDevicesKpiCalculatorFactory() {super();}

    // For unit testing purposes only
    @Inject
    public RegisteredDevicesKpiCalculatorFactory(TaskService taskService, RegisteredDevicesKpiService registeredDevicesKpiService, EventService eventService, Clock clock, TopologyService topologyService, DeviceService deviceService) {
        this();
        this.setTaskService(taskService);
        this.setRegisteredDevicesKpiService(registeredDevicesKpiService);
        this.setEventService(eventService);
        this.setDeviceService(deviceService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(
                new RegisteredDevicesKpiCalculator(registeredDevicesKpiService, eventService, deviceService, topologyService));
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setRegisteredDevicesKpiService(RegisteredDevicesKpiService registeredDevicesKpiService) {
        this.registeredDevicesKpiService = registeredDevicesKpiService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}