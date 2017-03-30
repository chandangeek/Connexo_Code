/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;

/**
 * @author sva
 * @since 15/07/2016 - 9:56
 */
public interface CompletionOptionsCallBack {

    /**
     * Send out a successful message to the {@link DestinationSpec} extracted from the {@link CompletionOptionsServiceCallDomainExtension} of
     * the given {@link ServiceCall}.
     *
     * @param serviceCall the {@link ServiceCall} linked to the HeadEndInterface operation,
     * which should have a extension of type {@link CompletionOptionsServiceCallDomainExtension}
     */
    void sendFinishedMessageToDestinationSpec(ServiceCall serviceCall);

    /**
     * Send out a message to the {@link DestinationSpec} extracted from the {@link CompletionOptionsServiceCallDomainExtension} of
     * the given {@link ServiceCall}, having the given {@link CompletionMessageInfo.CompletionMessageStatus}
     *
     * @param serviceCall the {@link ServiceCall} linked to the HeadEndInterface operation,
     * which should have a extension of type {@link CompletionOptionsServiceCallDomainExtension}
     * @param completionMessageStatus the status to use for the message
     */
    void sendFinishedMessageToDestinationSpec(ServiceCall serviceCall, CompletionMessageInfo.CompletionMessageStatus completionMessageStatus);

    /**
     * Send out a message to the {@link DestinationSpec} extracted from the {@link CompletionOptionsServiceCallDomainExtension} of
     * the given {@link ServiceCall}, having the given {@link CompletionMessageInfo.CompletionMessageStatus} and the given failureReason
     *
     * @param serviceCall the {@link ServiceCall} linked to the HeadEndInterface operation,
     * which should have a extension of type {@link CompletionOptionsServiceCallDomainExtension}
     * @param completionMessageStatus the status to use for the message
     * @param failureReason the String to use as failure information text
     */
    void sendFinishedMessageToDestinationSpec(ServiceCall serviceCall, CompletionMessageInfo.CompletionMessageStatus completionMessageStatus, CompletionMessageInfo.FailureReason failureReason);

}