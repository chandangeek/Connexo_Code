package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.impl.exceptions.LimitsExceededForCommandException;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;

@Component(name="com.energyict.mdc.device.command.CommandUpdateVetoHandler", service = TopicHandler.class, immediate = true)
public class CommandUpdateVetoHandler implements TopicHandler {
    private final CommandRuleService commandRuleService;
    private final Thesaurus thesaurus;

    @Inject
    public CommandUpdateVetoHandler(CommandRuleService commandRuleService, Thesaurus thesaurus) {
        this.commandRuleService = commandRuleService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceMessage deviceMessage = (DeviceMessage) localEvent.getSource();
        if(commandRuleService.limitsExceededForUpdatedCommand(deviceMessage)) {
            throw new LimitsExceededForCommandException(thesaurus, MessageSeeds.LIMITS_EXCEEDED);
        } else {
            commandRuleService.commandUpdated(deviceMessage);
        }
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/deviceMessage/UPDATED";
    }
}
