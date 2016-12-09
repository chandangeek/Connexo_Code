package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.rest.util.DualControlChangeInfo;

import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRuleInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public CommandRuleInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public CommandRuleInfo from(CommandRule commandRule) {
        CommandRuleInfo commandRuleInfo = new CommandRuleInfo();
        commandRuleInfo.id = commandRule.getId();
        commandRuleInfo.name = commandRule.getName();
        commandRuleInfo.dayLimit = commandRule.getDayLimit();
        commandRuleInfo.weekLimit = commandRule.getWeekLimit();
        commandRuleInfo.monthLimit = commandRule.getMonthLimit();
        commandRuleInfo.version = commandRule.getVersion();
        commandRuleInfo.commands = commandRule.getCommands()
                .stream()
                .map(commandInRule -> new CommandInfo(commandInRule.getCommand().getCategory().getName(), commandInRule.getCommand().getName(), commandInRule.getCommand().getId().name()))
                .sorted(CommandInfo::compareTo)
                .collect(Collectors.toList());
        commandRuleInfo.active = commandRule.isActive();


        if (commandRule.getCommandRulePendingUpdate().isPresent()) {
            CommandRulePendingUpdate pendingUpdate = commandRule.getCommandRulePendingUpdate().get();
            if (pendingUpdate.isActivation()) {
                commandRuleInfo.statusMessage = "pendingActivation";
            } else if (pendingUpdate.isDeactivation()) {
                commandRuleInfo.statusMessage = "pendingDeactivation";
            } else if (pendingUpdate.isRemoval()) {
                commandRuleInfo.statusMessage = "pendingRemoval";
            } else if (pendingUpdate.isUpdate()) {
                commandRuleInfo.statusMessage = "pendingUpdate";
            }
        }
        return commandRuleInfo;
    }

    public CommandRuleInfo createWithChanges(CommandRule commandRule) {
        CommandRuleInfo commandRuleInfo = from(commandRule);
        if (commandRule.getCommandRulePendingUpdate().isPresent()) {
            CommandRulePendingUpdate pendingUpdate = commandRule.getCommandRulePendingUpdate().get();
            DualControlInfo dualControlInfo = new DualControlInfo();
            List<DualControlChangeInfo> changes = new ArrayList<>();
            commandRuleInfo.dualControl = dualControlInfo;
            dualControlInfo.changes = changes;
            dualControlInfo.hasCurrentUserAccepted = commandRule.hasCurrentUserAccepted();
            dualControlInfo.pendingChangesType = PendingChangesType.getCorrectType(pendingUpdate);
            if (pendingUpdate.isRemoval()) {
                changes.add(new DualControlChangeInfo(translate(TranslationKeys.STATUS), translate(TranslationKeys.ACTIVE), translate(TranslationKeys.REMOVED)));
                return commandRuleInfo;
            }
            checkBasicChanges(changes, commandRule, pendingUpdate);
            checkCommandChanges(changes, commandRule, pendingUpdate);
        }
        return commandRuleInfo;
    }

    private void checkCommandChanges(List<DualControlChangeInfo> changes, CommandRule commandRule, CommandRulePendingUpdate pendingUpdate) {
        List<CommandInRule> copyOriginal = new ArrayList<>();
        List<CommandInRule> copyUpdate = new ArrayList<>();
        Collections.copy(commandRule.getCommands(), copyOriginal);
        Collections.copy(pendingUpdate.getCommands(), copyUpdate);
        copyUpdate.removeAll(commandRule.getCommands());
        copyOriginal.removeAll(pendingUpdate.getCommands());

        copyOriginal.stream()
                .forEach(commandInRule -> changes.add(
                        new DualControlChangeInfo(commandInRule.getCommand().getCategory().getName() + " - " + commandInRule.getCommand().getName(),
                                translate(TranslationKeys.YES),
                                translate(TranslationKeys.NO))));

        copyUpdate.stream()
                .forEach(commandInRule -> changes.add(
                        new DualControlChangeInfo(commandInRule.getCommand().getCategory().getName() + " - " + commandInRule.getCommand().getName(),
                                translate(TranslationKeys.NO),
                                translate(TranslationKeys.YES))));
    }

    private void checkBasicChanges(List<DualControlChangeInfo> changes, CommandRule commandRule, CommandRulePendingUpdate pendingUpdate) {
        if (commandRule.isActive() != pendingUpdate.isActive()) {
            changes.add(new DualControlChangeInfo(translate(TranslationKeys.STATUS),
                    translate(TranslationKeys.fromActive(commandRule.isActive())),
                    translate(TranslationKeys.fromActive(pendingUpdate.isActive()))));
        }

        if (!commandRule.getName().equals(pendingUpdate.getName())) {
            changes.add(new DualControlChangeInfo(translate(TranslationKeys.NAME), commandRule.getName(), pendingUpdate.getName()));
        }
        if (commandRule.getDayLimit() != pendingUpdate.getDayLimit()) {
            changes.add(new DualControlChangeInfo(translate(TranslationKeys.DAYLIMIT), String.valueOf(commandRule.getDayLimit()), String.valueOf(pendingUpdate.getDayLimit())));
        }
        if (commandRule.getWeekLimit() != pendingUpdate.getWeekLimit()) {
            changes.add(new DualControlChangeInfo(translate(TranslationKeys.WEEKLIMIT), String.valueOf(commandRule.getWeekLimit()), String.valueOf(pendingUpdate.getWeekLimit())));
        }
        if (commandRule.getMonthLimit() != pendingUpdate.getMonthLimit()) {
            changes.add(new DualControlChangeInfo(translate(TranslationKeys.MONTHLIMIT), String.valueOf(commandRule.getMonthLimit()), String.valueOf(pendingUpdate.getMonthLimit())));
        }
    }

    private String translate(TranslationKey key) {
        return thesaurus.getFormat(key).format();
    }
}
