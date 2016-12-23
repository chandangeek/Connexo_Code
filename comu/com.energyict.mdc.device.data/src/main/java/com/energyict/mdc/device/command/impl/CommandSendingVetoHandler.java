package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.MacException;

import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.impl.exceptions.InvalidCommandLimitationRulesMacException;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * If there are commands to be sent, the command limitation rules should be checked if they are still valid. So there are two possible exceptions -> mac exception and not valid exception
 */
@Component(name="com.energyict.mdc.device.command.CommandSendingVetoHandler", service = TopicHandler.class, immediate = true)
public class CommandSendingVetoHandler implements TopicHandler {

    private Thesaurus thesaurus;
    private volatile CommandRuleService commandRuleService;

    public CommandSendingVetoHandler() {

    }

    @Inject
    public CommandSendingVetoHandler(CommandRuleService commandRuleService, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        setCommandRuleService(commandRuleService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CommandRuleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setCommandRuleService(CommandRuleService commandRuleService) {
        this.commandRuleService = commandRuleService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ((CommandRuleServiceImpl) commandRuleService).checkCommandRuleStats();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/outboundcommunication/COMMANDSWILLBESENT";
    }
}
