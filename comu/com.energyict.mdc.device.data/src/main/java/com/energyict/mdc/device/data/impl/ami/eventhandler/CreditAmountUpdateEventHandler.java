/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.UpdateCreditAmountServiceCallHandler;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles events that are being sent when a {@link CreditAmount} has been created/updated/deleted.
 * The events are only of interest if:
 * <ul>
 * <li>they refer to a create or update operation</li>
 * <li>the {@link Device} having the credit amount was used as target of a {@link ServiceCall} used in the {@link MultiSenseHeadEndInterface} for an update credit amount operation</li>
 * </ul>
 * If this is the case, then the linked {@link ServiceCall} will be transited to state ONGOING (which will delegate further processing to the
 * {@link ServiceCallHandler} of the {@link ServiceCall}).
 */
@Component(name = "com.energyict.mdc.device.data.ami.creditamount.update.eventhandler", service = TopicHandler.class, immediate = true)
public class CreditAmountUpdateEventHandler implements TopicHandler {
    private static final String TOPIC = "com/energyict/mdc/device/data/creditamount/*";

    private volatile ServiceCallService serviceCallService;

    public CreditAmountUpdateEventHandler() {
        // for OSGi
    }

    // for unit test
    @Inject
    public CreditAmountUpdateEventHandler(ServiceCallService serviceCallService) {
        setServiceCallService(serviceCallService);
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
        CreditAmount creditAmount = (CreditAmount) event.getSource();
        List<ServiceCall> serviceCalls = findAllUpdateCreditAmountServiceCallsLinkedTo(creditAmount.getDevice());
        for (ServiceCall serviceCall : serviceCalls) {
            handle(serviceCall, creditAmount);
        }
    }

    private void handle(ServiceCall serviceCall, CreditAmount creditAmount) {
        if (creditAmountUpdatedAsPartOfUpdateCreditAmountOperation(serviceCall, creditAmount)) {
            serviceCall.transitionWithLockIfPossible(DefaultState.ONGOING);
        }
    }

    private boolean creditAmountUpdatedAsPartOfUpdateCreditAmountOperation(ServiceCall serviceCall, CreditAmount creditAmount) {
        CommandServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CommandCustomPropertySet()).get();
        return CommandOperationStatus.READ_STATUS_INFORMATION.equals(domainExtension.getCommandOperationStatus()) && domainExtension.getReleaseDate().isBefore(creditAmount.getLastChecked());
        // Else the creditAmount update was not triggered by ServiceCall (but was manually triggered or executed according to its schedule)
        // In such case, the update should be ignored here
    }

    private List<ServiceCall> findAllUpdateCreditAmountServiceCallsLinkedTo(Device device) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.targetObjects.add(device);
        filter.types = Collections.singletonList(UpdateCreditAmountServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        filter.states = Arrays.stream(DefaultState.values())
                .filter(DefaultState::isOpen)
                .map(DefaultState::name)
                .collect(Collectors.toList());
        return serviceCallService.getServiceCallFinder(filter).find();
    }
}
