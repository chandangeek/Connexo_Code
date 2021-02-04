/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.UpdateDeviceConnectionPropertyEvent;
import com.energyict.mdc.engine.impl.meterdata.DeviceConnectionProperty;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will update the connection property (host, portNumber...) of a {@link com.energyict.mdc.upl.meterdata.Device device}
 * from information that was collected during the device communication session.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:48)
 */
public class UpdateDeviceConnectionProperty extends DeviceCommandImpl<UpdateDeviceConnectionPropertyEvent> {

    public static final String DESCRIPTION_TITLE = "Update device connection property";

    private DeviceIdentifier deviceIdentifier;
    protected Map<String, Object> connectionPropertyNameAndValue;
    protected ConnectionTask connectionTask;

    public UpdateDeviceConnectionProperty(DeviceConnectionProperty connectionProperty, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceIdentifier = connectionProperty.getDeviceIdentifier();
        this.connectionTask = connectionProperty.getConnectionTask();
        this.connectionPropertyNameAndValue = connectionProperty.getConnectionPropertyNameAndValue();
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.updateConnectionTaskProperties(this.connectionTask, this.connectionPropertyNameAndValue);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
            connectionPropertyNameAndValue.forEach((key, value) -> {
                builder.addProperty("connection property name").append(key);
                builder.addProperty("connection property value").append(value);
            });
        }
    }

    protected Optional<UpdateDeviceConnectionPropertyEvent> newEvent(List<Issue> issues) {
        UpdateDeviceConnectionPropertyEvent event = new UpdateDeviceConnectionPropertyEvent(new ComServerEventServiceProvider(), this.deviceIdentifier);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}