/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command;


import com.energyict.mdc.device.command.impl.exceptions.ExceededCommandRule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link CommandRule}s.
 */
@ProviderType
public interface CommandRuleService {

    String COMPONENT_NAME = "CLR";

    /**
     * Finds all the CommandRules
     *
     * @return List of CommandRules
     */
    List<CommandRule> findAllCommandRules();

    CommandRuleBuilder createRule(String name);

    Optional<CommandRule> findCommandRule(long commandRuleId);

    Optional<CommandRule> findAndLockCommandRule(long commandRuleId, long version);

    Optional<CommandRule> findCommandRuleByName(String name);

    Optional<CommandRulePendingUpdate> findCommandTemplateRuleByName(String name);

    void deleteRule(CommandRule commandRule);

    void commandCreated(DeviceMessage deviceMessage);

    List<ExceededCommandRule>  limitsExceededForUpdatedCommand(DeviceMessage deviceMessage, Instant oldReleaseDate);

    List<ExceededCommandRule>  limitsExceededForNewCommand(DeviceMessage deviceMessage);

    void commandUpdated(DeviceMessage deviceMessage, Instant oldReleaseDate);

    void commandDeleted(DeviceMessage deviceMessage);

    List<ICommandRuleCounter> getCurrentCounters(CommandRule commandRule);


    @ProviderType
    interface CommandRuleBuilder {
        CommandRuleBuilder dayLimit(long dayLimit);
        CommandRuleBuilder weekLimit(long weekLimit);
        CommandRuleBuilder monthLimit(long monthLimit);
        CommandRuleBuilder command(String name);
        CommandRule add();
    }
}
