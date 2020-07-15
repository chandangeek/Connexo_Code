/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

import ch.iec.tc57._2011.schema.message.ErrorType;

import java.util.ArrayList;
import java.util.List;

public class EndDeviceControlsRequestMessage {
    private String correlationId;
    private String replyAddress;
    private Long maxExecTimeout;

    private List<EndDeviceControlMessage> endDeviceControlMessages = new ArrayList<>();
    private List<ErrorType> errorTypes = new ArrayList<>();

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getReplyAddress() {
        return replyAddress;
    }

    public void setReplyAddress(String replyAddress) {
        this.replyAddress = replyAddress;
    }

    public Long getMaxExecTimeout() {
        return maxExecTimeout;
    }

    public void setMaxExecTimeout(Long maxExecTimeout) {
        this.maxExecTimeout = maxExecTimeout;
    }

    public List<EndDeviceControlMessage> getEndDeviceControlMessages() {
        return endDeviceControlMessages;
    }

    public void addEndDeviceControlMessage(EndDeviceControlMessage endDeviceControlMessage) {
        endDeviceControlMessages.add(endDeviceControlMessage);
    }

    public List<ErrorType> getErrorTypes() {
        return errorTypes;
    }

    public void addErrorType(ErrorType errorType) {
        this.errorTypes.add(errorType);
    }

    public void addErrorTypes(List<ErrorType> errorTypes) {
        this.errorTypes.addAll(errorTypes);
    }
}
