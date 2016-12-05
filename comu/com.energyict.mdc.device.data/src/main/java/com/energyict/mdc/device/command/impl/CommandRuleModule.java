package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class CommandRuleModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(DeviceMessageSpecificationService.class);
        bind(CommandRuleServiceImpl.class).in(Scopes.SINGLETON);
        bind(CommandRuleService.class).to(CommandRuleServiceImpl.class);
    }
}
