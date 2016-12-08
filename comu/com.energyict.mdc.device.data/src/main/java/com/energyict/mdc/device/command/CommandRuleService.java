package com.energyict.mdc.device.command;


import aQute.bnd.annotation.ProviderType;

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

    Optional<CommandRule> findCommandRuleByName(String name);

    Optional<CommandRulePendingUpdate> findCommandTemplateRuleByName(String name);

    void deleteRule(CommandRule commandRule);


    @ProviderType
    interface CommandRuleBuilder {
        CommandRuleBuilder dayLimit(long dayLimit);
        CommandRuleBuilder weekLimit(long weekLimit);
        CommandRuleBuilder monthLimit(long monthLimit);
        CommandRuleBuilder command(String name);
        CommandRule add();
    }
}
