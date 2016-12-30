package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.rest.util.DualControlChangeInfo;

import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;
import com.energyict.mdc.device.command.CommandRuleService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRuleInfoFactory {
    private final Thesaurus thesaurus;
    private final CommandRuleService commandRuleService;

    @Inject
    public CommandRuleInfoFactory(Thesaurus thesaurus, CommandRuleService commandRuleService) {
        this.thesaurus = thesaurus;
        this.commandRuleService = commandRuleService;
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



        commandRuleInfo.availableActions = EnumSet.allOf(AvailableActions.class);
        if(commandRuleInfo.active) {
            commandRuleInfo.availableActions.remove(AvailableActions.ACTIVATE);
            addCurrentCounts(commandRule, commandRuleInfo);
        } else {
            commandRuleInfo.availableActions.remove(AvailableActions.DEACTIVATE);
        }
        if (commandRule.getCommandRulePendingUpdate().isPresent()) {
            CommandRulePendingUpdate pendingUpdate = commandRule.getCommandRulePendingUpdate().get();
            if (pendingUpdate.isActivation()) {
                commandRuleInfo.availableActions.remove(AvailableActions.ACTIVATE);
                commandRuleInfo.statusMessage = translate(TranslationKeys.PENDING_ACTIVATION);
            } else if (pendingUpdate.isDeactivation()) {
                commandRuleInfo.availableActions.remove(AvailableActions.DEACTIVATE);
                commandRuleInfo.statusMessage = translate(TranslationKeys.PENDING_DEACTIVATION);;
            } else if (pendingUpdate.isRemoval()) {
                commandRuleInfo.availableActions.remove(AvailableActions.REMOVE);
                commandRuleInfo.statusMessage = translate(TranslationKeys.PENDING_REMOVAL);
            } else if (pendingUpdate.isUpdate()) {
                commandRuleInfo.statusMessage = translate(TranslationKeys.PENDING_UPDATE);
            }
        }

        return commandRuleInfo;
    }

    private void addCurrentCounts(CommandRule commandRule, CommandRuleInfo commandRuleInfo) {
        commandRuleInfo.currentCounts = new ArrayList<>();
        commandRuleService.getCurrentCounters(commandRule)
                .stream()
                .forEach(counter -> {
                    CurrentCountInfo info = new CurrentCountInfo();
                    info.currentCount = counter.getCount();
                    info.from = counter.getFrom();
                    info.to = counter.getTo();
                    info.type = counter.getCounterType().name();
                    commandRuleInfo.currentCounts.add(info);
                });
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
        List<CommandInRule> copyOriginal = new ArrayList<>(commandRule.getCommands());
        List<CommandInRule> copyUpdate = new ArrayList<>(pendingUpdate.getCommands());
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
