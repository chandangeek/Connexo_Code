package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class CommandRuleModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(DeviceMessageSpecificationService.class);
        requireBinding(DataVaultService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(UpgradeService.class);
        requireBinding(UserService.class);
        requireBinding(DualControlService.class);
        requireBinding(DeviceMessageService.class);
        bind(CommandRuleServiceImpl.class).in(Scopes.SINGLETON);
        bind(CommandRuleService.class).to(CommandRuleServiceImpl.class);
    }
}
