/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_DEVICE(1, "NoSuchDevice", "No device with name ''{0}''."),
    NO_REGISTERED_NOTIFICATION_ENDPOINT(2, "NoRegisteredNotificationEndPoint", "No registered notification end point is found by id ''{0}''."),
    DEVICE_ID_ATTRIBUTE_IS_NOT_SET(3, "DeviceIdAttributeIsNotSet", "''Device identifier'' attribute isn''t set on Device SAP info CAS."),
    NO_LRN(4, "NoLrn", "No LRN is available on current or future data sources on the device."),
    DEVICE_ALREADY_REGISTERED(5, "DeviceAlreadyRegistered", "Device already registered (Registered flag is true on Device SAP info CAS)."),
    REQUEST_SENDING_HAS_FAILED(6, "RequestSendingHasFailed", "The request sending has failed. See web service history for details."),
    NO_SUCH_MESSAGE_QUEUE(7, "NoSuchMessageQueue", "Unable to queue command: no message queue was found"),
    NO_APPSERVER(8, "NoAppServer", "There is no active application server that can handle this request"),
    BAD_ACTION(9, "BadAction", "Expected action to be either ''add'' or ''remove''."),
    ;

    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return SapApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
