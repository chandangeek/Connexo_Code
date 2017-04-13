/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.processes.keyrenewal.api.servicecall.KeyRenewalCustomPropertySet;
import com.energyict.mdc.processes.keyrenewal.api.servicecall.KeyRenewalDomainExtension;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.joda.time.DateTimeConstants;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.text.MessageFormat;


public class CompletionOptionsHandler implements MessageHandler {

    private final JsonService jsonService;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final BpmService bpmService;

    public CompletionOptionsHandler(JsonService jsonService, ServiceCallService serviceCallService, Thesaurus thesaurus, BpmService bpmService) {
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.bpmService = bpmService;
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

            KeyRenewalDomainExtension domainExtension = serviceCall.getExtensionFor(new KeyRenewalCustomPropertySet()).get();
            if (!domainExtension.providedResponse()) {
                String callbackUri;

                if (completionMessageInfo.getCompletionMessageStatus().equals(CompletionMessageInfo.CompletionMessageStatus.SUCCESS)) {
                    callbackUri = domainExtension.getCallbackSuccess();
                    if (callbackUri == null || callbackUri.isEmpty()) {
                        throw new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.CALL_BACK_SUCCESS_URI_NOT_SPECIFIED).format());
                    }
                } else {
                    callbackUri = domainExtension.getCallbackError();
                    if (callbackUri == null || callbackUri.isEmpty()) {
                        throw new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.CALL_BACK_ERROR_URI_NOT_SPECIFIED).format());
                    }
                }


                ResponseInfo responseInfo = new ResponseInfo();
                responseInfo.status = completionMessageInfo.getCompletionMessageStatus();
                responseInfo.reason = completionMessageInfo.getFailureReason();

                this.bpmService.getBpmServer().doPost(callbackUri, responseInfo.toString());

                domainExtension.setProvidedResponse(true);
                serviceCall.update(domainExtension);
                serviceCall.log(LogLevel.INFO, MessageFormat.format("Provided response to Flow: {0}", completionMessageInfo.getCompletionMessageStatus()));
            } // Else, a response has already been send out
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