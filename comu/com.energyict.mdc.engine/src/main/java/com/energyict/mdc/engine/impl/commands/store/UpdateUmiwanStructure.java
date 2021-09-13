/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.UpdateUmiwanStructueEvent;
import com.energyict.mdc.engine.impl.meterdata.UmiwanStructure;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UpdateUmiwanStructure extends DeviceCommandImpl<UpdateUmiwanStructueEvent> {

    public static final String DESCRIPTION_TITLE = "Update umiwan structure";

    private MessageIdentifier messageIdentifier;
    private Map<String, Object> properties;
    private String cas;

    public UpdateUmiwanStructure(UmiwanStructure umiwanConfiguration, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider, String cas) {
        super(comTaskExecution, serviceProvider);
        this.messageIdentifier = umiwanConfiguration.getMessageIdentifier();
        this.properties = umiwanConfiguration.getProperties();
        this.cas = cas;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.updateUmiwanStructure(this.getComTaskExecution(), this.properties, cas);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("messageIdentifier").append(this.messageIdentifier);
            properties.forEach((key, value) -> {
                builder.addProperty("umiwan structure property name").append(key);
                builder.addProperty("umiwan structure property value").append(value);
            });
        }
    }

    protected Optional<UpdateUmiwanStructueEvent> newEvent(List<Issue> issues) {
        UpdateUmiwanStructueEvent event = new UpdateUmiwanStructueEvent(new ComServerEventServiceProvider(), this.messageIdentifier);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}