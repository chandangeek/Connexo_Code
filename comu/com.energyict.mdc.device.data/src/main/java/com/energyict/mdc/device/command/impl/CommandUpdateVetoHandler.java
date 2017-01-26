package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.MacException;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.impl.exceptions.ExceededCommandRule;
import com.energyict.mdc.device.command.impl.exceptions.InvalidCommandLimitationRulesMacException;
import com.energyict.mdc.device.command.impl.exceptions.LimitsExceededForCommandException;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.List;

@Component(name = "com.energyict.mdc.device.command.CommandUpdateVetoHandler", service = TopicHandler.class, immediate = true)
public class CommandUpdateVetoHandler implements TopicHandler {
    private volatile CommandRuleService commandRuleService;
    private volatile Thesaurus thesaurus;

    public CommandUpdateVetoHandler() {

    }

    @Inject
    public CommandUpdateVetoHandler(CommandRuleService commandRuleService, Thesaurus thesaurus) {
        setCommandRuleService(commandRuleService);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.SERVICE);
    }

    @Reference
    public void setCommandRuleService(CommandRuleService commandRuleService) {
        this.commandRuleService = commandRuleService;
    }

    @Override
    public void handle(LocalEvent localEvent) {

        try {
            DeviceMessage deviceMessage = (DeviceMessage) localEvent.getSource();

            if (deviceMessage.getStatus().equals(DeviceMessageStatus.CANCELED)) {
                commandRuleService.commandDeleted(deviceMessage);
            } else {
                long oldReleaseDate = (Long) localEvent.toOsgiEvent().getProperty("oldReleaseDate");
                List<ExceededCommandRule> exceededCommandRules = commandRuleService.limitsExceededForUpdatedCommand(deviceMessage, Instant.ofEpochMilli(oldReleaseDate));
                if (!exceededCommandRules.isEmpty()) {
                    throw new LimitsExceededForCommandException(thesaurus, exceededCommandRules);
                } else {
                    commandRuleService.commandUpdated(deviceMessage, Instant.ofEpochMilli(oldReleaseDate));
                }
            }
        } catch (MacException e) {
            throw new InvalidCommandLimitationRulesMacException(thesaurus, MessageSeeds.MAC_COMMAND_RULES_FAILED);
        }

    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/deviceMessage/UPDATED";
    }
}
