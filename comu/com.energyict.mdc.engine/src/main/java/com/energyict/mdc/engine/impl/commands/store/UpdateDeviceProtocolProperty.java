/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.UpdateDeviceProtocolPropertyEvent;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolProperty;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;

import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will update a protocol property of a Device
 * from information that was collected during the device communication session.
 *
 * @author sva
 * @since 16/10/2014 - 16:19
 */
public class UpdateDeviceProtocolProperty extends DeviceCommandImpl<UpdateDeviceProtocolPropertyEvent> {

    public static final String DESCRIPTION_TITLE = "Update device protocol property";

    private final DeviceIdentifier deviceIdentifier;
    private final String propertyName;
    private final Object propertyValue;

    public UpdateDeviceProtocolProperty(DeviceProtocolProperty deviceProtocolProperty, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceIdentifier = deviceProtocolProperty.getDeviceIdentifier();
        this.propertyName = deviceProtocolProperty.getPropertyName();
        this.propertyValue = deviceProtocolProperty.getPropertyValue();
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        try {
            //TODO port EISERVERSG-3724
            if (comServerDAO.findOfflineDevice(deviceIdentifier, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG)).isPresent()) {
                try {
                    comServerDAO.updateDeviceProtocolProperty(deviceIdentifier, propertyName, propertyValue);
                } catch (DeviceProtocolPropertyException e) {
                    this.addIssue(
                            CompletionCode.ConfigurationWarning,
                            this.getIssueService().newWarning(this, MessageSeeds.PROPERTY_VALIDATION_FAILED, propertyName, propertyValue));
                }
            }
        } catch (CanNotFindForIdentifier e) {
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(deviceIdentifier, e.getMessageSeed(), deviceIdentifier));
        }
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
            builder.addProperty("property").append(propertyName);
            builder.addProperty("value").append(propertyValue.toString());
        }
    }

    @Override
    protected Optional<UpdateDeviceProtocolPropertyEvent> newEvent(List<Issue> issues) {
        UpdateDeviceProtocolPropertyEvent event = new UpdateDeviceProtocolPropertyEvent(new ComServerEventServiceProvider(),
                deviceIdentifier,
                propertyName,
                propertyValue);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}