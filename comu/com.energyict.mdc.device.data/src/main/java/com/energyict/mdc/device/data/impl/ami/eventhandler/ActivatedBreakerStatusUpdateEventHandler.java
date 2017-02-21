/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.ArmServiceCallHandler;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.ConnectServiceCallHandler;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.DisconnectServiceCallHandler;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

/**
 * Handles events that are being sent when a {@link ActivatedBreakerStatus} has been created/updated/deleted.
 * The events are only of interest if:
 * <ul>
 * <li>they refer to a create or update operation</li>
 * <li>the {@link Device} having the breaker status was used as target of a {@link ServiceCall} used in the {@link MultiSenseHeadEndInterface} for a contactor operation</li>
 * </ul>
 * If this is the case, then the linked {@link ServiceCall} will be transited to state ONGOING (which will delegate furtherprocessingg to the
 * {@link ServiceCallHandler} of the {@link ServiceCall}
 *
 * @author sva
 * @since 14/06/2016 - 9:59
 */
@Component(name = "com.energyict.mdc.device.data.ami.breakerstatus.update.eventhandler", service = TopicHandler.class, immediate = true)
public class ActivatedBreakerStatusUpdateEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/data/activatedbreakerstatus/*";

    private volatile ServiceCallService serviceCallService;

    public ActivatedBreakerStatusUpdateEventHandler() {
    }

    public ActivatedBreakerStatusUpdateEventHandler(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        if (event.getType().getTopic().endsWith(EventType.ACTIVATED_BREAKER_STATUS_CREATED.topic()) || event.getType().getTopic().endsWith(EventType.ACTIVATED_BREAKER_STATUS_UPDATED.topic())) {
            ActivatedBreakerStatus breakerStatus = (ActivatedBreakerStatus) event.getSource();
            List<ServiceCall> serviceCalls = findAllBreakerOperationServiceCallsLinkedTo(breakerStatus.getDevice());
            for (ServiceCall serviceCall : serviceCalls) {
                handle(serviceCall, breakerStatus);
            }
        }
    }

    private void handle(ServiceCall serviceCall, ActivatedBreakerStatus breakerStatus) {
        if (breakerStatusUpdatedAsPartOfContactorOperation(serviceCall, breakerStatus)) {
            if (serviceCall.canTransitionTo(DefaultState.ONGOING)) {
                serviceCall.requestTransition(DefaultState.ONGOING);
            }
        }
    }

    private boolean breakerStatusUpdatedAsPartOfContactorOperation(ServiceCall serviceCall, ActivatedBreakerStatus breakerStatus) {
        CommandServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CommandCustomPropertySet()).get();
        return CommandOperationStatus.READ_STATUS_INFORMATION.equals(domainExtension.getCommandOperationStatus()) && domainExtension.getReleaseDate().isBefore(breakerStatus.getLastChecked());
        // Else the breakerStatus update was not triggered by ServiceCall (but was manually triggered or executed according to its schedule)
        // In such case, the update should be ignored here
    }

    private List<ServiceCall> findAllBreakerOperationServiceCallsLinkedTo(Device device) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.targetObject = device;
        filter.types = Arrays.asList(
                DisconnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                ConnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME,
                ArmServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        return serviceCallService.getServiceCallFinder(filter).find();
    }
}