/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validates that a pending {@link DeviceMessage} is still valid since the time it was created.
 * A message with a calendar parameter that was removed from the device type
 * is an example of a message that has become invalid since its creation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-16 (12:58)
 */
class PendingMessagesValidator {

    private final Device device;

    PendingMessagesValidator(Device device) {
        this.device = device;
    }

    boolean isStillValid(DeviceMessage message) {
        if (this.hasCalendarAttribute(message)) {
            Set<Calendar> allowedCalendars = this.allowedCalendars(this.device);
            return this.calendarAttributeValues(message)
                    .stream()
                    .allMatch(allowedCalendars::contains);
        } else {
            return true;
        }
    }

    String failingCalendarNames(DeviceMessage message) {
        Set<Calendar> allowedCalendars = this.allowedCalendars(this.device);
        return this.calendarAttributeValues(message)
                .stream()
                .filter(Predicates.not(allowedCalendars::contains))
                .map(Calendar::getName)
                .collect(Collectors.joining(", "));
    }

    private boolean hasCalendarAttribute(DeviceMessage message) {
        return Stream.of(DeviceMessageId.values())
                .filter(deviceMessage -> deviceMessage.dbValue() == message.getMessageId())
                .findAny().map(deviceMessageId -> this.messagesWithCalendarAttributes().contains(message.getDeviceMessageId()))
                .orElse(Boolean.FALSE);
    }

    private Set<DeviceMessageId> messagesWithCalendarAttributes() {
        return EnumSet.of(
                DeviceMessageId.ACTIVITY_CALENDER_SEND,
                DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME,
                DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE,
                DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_SEND,
                DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATE,
                DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE,
                DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND,
                DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE,
                DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT);
    }

    private Set<Calendar> allowedCalendars(Device device) {
        return device
                .getDeviceType()
                .getAllowedCalendars()
                .stream()
                .map(AllowedCalendar::getCalendar)
                .flatMap(Functions.asStream())
                .collect(Collectors.toSet());
    }

    private List<Calendar> calendarAttributeValues(DeviceMessage message) {
        return message
                .getAttributes()
                .stream()
                .map(DeviceMessageAttribute.class::cast)        //Downcast to Connexo DeviceMessageAttribute
                .filter(this::isCalendarRelated)
                .map(DeviceMessageAttribute::getValue)
                .map(Calendar.class::cast)
                .collect(Collectors.toList());
    }

    private boolean isCalendarRelated(DeviceMessageAttribute attribute) {
        return attribute.getSpecification().isReference()
                && Calendar.class.isAssignableFrom(attribute.getSpecification().getValueFactory().getValueType());
    }

}