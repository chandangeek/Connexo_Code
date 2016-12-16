package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.device.command.CommandDeletedTopicHandler", service = TopicHandler.class, immediate = true)
public class CommandDeletedTopicHandler implements TopicHandler {
    private volatile CommandRuleService commandRuleService;

    public CommandDeletedTopicHandler() {

    }

    @Inject
    public CommandDeletedTopicHandler(CommandRuleService commandRuleService) {
        setCommandRuleService(commandRuleService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceMessage deviceMessage = (DeviceMessage) localEvent.getSource();
        commandRuleService.commandDeleted(deviceMessage);
    }


    @Reference
    public void setCommandRuleService(CommandRuleService commandRuleService) {
        this.commandRuleService = commandRuleService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/deviceMessage/DELETED";
    }
}
