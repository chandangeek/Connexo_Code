/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.UpdateDeviceConnectionPropertyEvent;
import com.energyict.mdc.engine.impl.meterdata.DeviceConnectionProperty;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.List;
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
    protected Object propertyValue;
    protected ConnectionTask connectionTask;
    protected String connectionTaskPropertyName;

    public UpdateDeviceConnectionProperty(DeviceConnectionProperty connectionProperty, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceIdentifier = connectionProperty.getDeviceIdentifier();
        this.propertyValue = connectionProperty.getPropertyValue();
        this.connectionTask = connectionProperty.getConnectionTask();
        this.connectionTaskPropertyName = connectionProperty.getConnectionTaskPropertyName();
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.updateConnectionTaskProperty(this.propertyValue, this.connectionTask, this.connectionTaskPropertyName);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
            builder.addProperty("connection property name").append(this.connectionTaskPropertyName);
            builder.addProperty("connection property value").append(this.propertyValue);
        }
    }

    protected Optional<UpdateDeviceConnectionPropertyEvent> newEvent(List<Issue> issues) {
        UpdateDeviceConnectionPropertyEvent event  =  new UpdateDeviceConnectionPropertyEvent(new ComServerEventServiceProvider(), this.deviceIdentifier);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}