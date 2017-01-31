/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.prepayment.impl.servicecall.ContactorOperationCustomPropertySet;
import com.elster.jupiter.prepayment.impl.servicecall.ContactorOperationDomainExtension;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.joda.time.DateTimeConstants;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import java.text.MessageFormat;


public class CompletionOptionsHandler implements MessageHandler {

    private final JsonService jsonService;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;

    public CompletionOptionsHandler(JsonService jsonService, ServiceCallService serviceCallService, Thesaurus thesaurus) {
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void process(Message message) {
        CompletionMessageInfo completionMessageInfo = jsonService.deserialize(message.getPayload(), CompletionMessageInfo.class);
        if (completionMessageInfo != null) {
            ServiceCall serviceCall = findServiceCall(completionMessageInfo.getCorrelationId());
            if (completionMessageInfo.getCompletionMessageStatus().equals(CompletionMessageInfo.CompletionMessageStatus.SUCCESS) && !serviceCall.getState().equals(DefaultState.SUCCESSFUL)) {
                // In case the parent service call is not yet marked as success, then do not send out the response
                // This is the case when we receive confirmation that one of the child service calls has completed successfully, but that there are still other child service calls ongoing
                return;
            }

            ContactorOperationDomainExtension domainExtension = serviceCall.getExtensionFor(new ContactorOperationCustomPropertySet()).get();
            if (!domainExtension.providedResponse()) {
                String callbackUri = domainExtension.getCallback();
                if (callbackUri == null || callbackUri.isEmpty()) {
                    throw new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.CALL_BACK_URI_NOT_SPECIFIED).format());
                }

                ResponseInfo responseInfo = new ResponseInfo();
                responseInfo.status = completionMessageInfo.getCompletionMessageStatus();
                responseInfo.reason = completionMessageInfo.getFailureReason();

                newJerseyClient().
                        target(callbackUri).
                        request().
                        post(Entity.json(responseInfo));

                domainExtension.setProvidedResponse(true);
                serviceCall.update(domainExtension);
                serviceCall.log(LogLevel.INFO, MessageFormat.format("Provided response to Redknee: {0}", responseInfo.toString()));

                if (completionMessageInfo.getCompletionMessageStatus().equals(CompletionMessageInfo.CompletionMessageStatus.SUCCESS)) {
                    // Only if the full operation was successful, update the status of the UsagePoint
                    updateStatusOfUsagePoint(serviceCall);
                }
            } // Else, a response has already been send out
        }
    }

    private void updateStatusOfUsagePoint(ServiceCall serviceCall) {
        UsagePoint usagePoint = (UsagePoint) serviceCall.getTargetObject().get();
        ContactorOperationDomainExtension contactorOperationDomainExtension = serviceCall.getExtensionFor(new ContactorOperationCustomPropertySet()).get();
        if (contactorOperationDomainExtension.getBreakerStatus() != null) {
            switch (contactorOperationDomainExtension.getBreakerStatus()) {
                case connected:
                    usagePoint.setConnectionState(ConnectionState.CONNECTED);
                    serviceCall.log(LogLevel.INFO, MessageFormat.format("Updated the usage point connection state to {0}", ConnectionState.CONNECTED.getName()));
                    break;
                case disconnected:
                    usagePoint.setConnectionState(ConnectionState.PHYSICALLY_DISCONNECTED);
                    serviceCall.log(LogLevel.INFO, MessageFormat.format("Updated the usage point connection state to {0}", ConnectionState.PHYSICALLY_DISCONNECTED.getName()));
                    break;
                case armed:
                    usagePoint.setConnectionState(ConnectionState.LOGICALLY_DISCONNECTED);
                    serviceCall.log(LogLevel.INFO, MessageFormat.format("Updated the usage point connection state to {0}", ConnectionState.LOGICALLY_DISCONNECTED.getName()));
                    break;
                default:
                    // Indicating the breaker state was not updated (~ only load limit operation handled)
                    // In this case the UsagePoint connection status should off course not be touched
                    break;
            }
        }
    }

    @Override
    public void onMessageDelete(Message message) {
        // No implementation needed
    }

    protected Client newJerseyClient() {
        return ClientBuilder.newClient().
                register(new JacksonFeature()).
                property(ClientProperties.CONNECT_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 5).
                property(ClientProperties.READ_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 2);
    }

    private ServiceCall findServiceCall(String serviceCallId) {
        return serviceCallService.getServiceCall(Long.parseLong(serviceCallId))
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL).format(serviceCallId)));
    }
}