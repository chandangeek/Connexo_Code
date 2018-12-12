/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

public class MessageAdapterMappingImpl implements MessageAdapterMapping {

    private String deviceProtocolJavaClassName;
    private String messageAdapterJavaClassName;

    // For persistence framework only
    public MessageAdapterMappingImpl() {
        super();
    }

    public MessageAdapterMappingImpl(String deviceProtocolJavaClassName, String messageAdapterJavaClassName) {
        super();
        this.deviceProtocolJavaClassName = deviceProtocolJavaClassName;
        this.messageAdapterJavaClassName = messageAdapterJavaClassName;
    }

    @Override
    public String getDeviceProtocolJavaClassName() {
        return deviceProtocolJavaClassName;
    }

    @Override
    public String getMessageAdapterJavaClassName() {
        return messageAdapterJavaClassName;
    }

}
