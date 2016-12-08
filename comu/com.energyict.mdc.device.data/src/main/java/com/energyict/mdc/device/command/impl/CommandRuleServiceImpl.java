package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.concurrent.DelayedRegistrationHandler;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;
import com.energyict.mdc.device.command.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.device.command", service = {CommandRuleService.class, TranslationKeyProvider.class, MessageSeedProvider.class}, property = {"name=" + CommandRuleService.COMPONENT_NAME}, immediate = true)
public class CommandRuleServiceImpl implements CommandRuleService, TranslationKeyProvider, MessageSeedProvider {
    private static final Logger LOGGER = Logger.getLogger(CommandRuleServiceImpl.class.getName());
    private volatile BundleContext context;

    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile Thesaurus thesaurus;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UpgradeService upgradeService;
    private volatile UserService userService;
    private volatile DualControlService dualControlService;

    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;

    private final DelayedRegistrationHandler delayedNotifications = new DelayedRegistrationHandler();
    public CommandRuleServiceImpl() {

    }

    @Inject
    CommandRuleServiceImpl(OrmService ormService, NlsService nlsService, BundleContext bundleContext, ThreadPrincipalService threadPrincipalService, DeviceMessageSpecificationService deviceMessageSpecificationService, UpgradeService upgradeService, UserService userService, DualControlService dualControlService) {
        this();
        setThreadPrincipalService(threadPrincipalService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setUpgradeService(upgradeService);
        setUserService(userService);
        setDualControlService(dualControlService);
        activate(bundleContext);
    }

    @Override
    public List<CommandRule> findAllCommandRules() {
        return dataModel.mapper(CommandRule.class).find();
    }

    @Override
    public CommandRuleBuilder createRule(String name) {
        return new CommandRuleBuilderImpl(name);
    }

    @Override
    public Optional<CommandRule> findCommandRule(long commandRuleId) {
        List<CommandRule> commandRules = dataModel.mapper(CommandRule.class).select(where("id").isEqualToIgnoreCase(commandRuleId));
        return commandRules.isEmpty() ? Optional.empty() : Optional.of(commandRules.get(0));
    }

    @Override
    public Optional<CommandRule> findCommandRuleByName(String name) {
        List<CommandRule> commandRules = dataModel.mapper(CommandRule.class).select(where(CommandRuleImpl.Fields.NAME.fieldName()).isEqualToIgnoreCase(name));
        return commandRules.isEmpty() ? Optional.empty() : Optional.of(commandRules.get(0));
    }

    @Override
    public Optional<CommandRulePendingUpdate> findCommandTemplateRuleByName(String name) {
        List<CommandRulePendingUpdate> commandRuleTemplates = dataModel.mapper(CommandRulePendingUpdate.class).select(where(CommandRulePendingUpdateImpl.Fields.NAME.fieldName()).isEqualToIgnoreCase(name));
        return commandRuleTemplates.isEmpty() ? Optional.empty() : Optional.of(commandRuleTemplates.get(0));
    }

    @Override
    public void deleteRule(CommandRule commandRule) {
        ((CommandRuleImpl) commandRule).delete();
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return CommandRuleService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(Privileges.values());
    }

    @Activate
    public void activate(BundleContext context) {
        CommandRuleService commandRuleService = this;
        LOGGER.info(() -> "Activating " + this.toString() + " from thread " + Thread.currentThread().getName());
        try {
            this.context = context;

            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(DataModel.class).toInstance(dataModel);
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                    bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                    bind(UserService.class).toInstance(userService);
                    bind(CommandRuleService.class).toInstance(commandRuleService);
                    bind(DualControlService.class).toInstance(dualControlService);
                }
            });

            upgradeService.register(identifier("MultiSense", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());

            delayedNotifications.ready();
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel("CLR", "MultiSense Command limitation rule");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CommandRuleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setDualControlService(DualControlService dualControlService) {
        this.dualControlService = dualControlService;
    }

    private class CommandRuleBuilderImpl implements CommandRuleBuilder {
        private CommandRuleImpl commandRule;

        public CommandRuleBuilderImpl(String name) {
            this.commandRule = new CommandRuleImpl(dataModel, deviceMessageSpecificationService, dualControlService);
            commandRule.setName(name);
        }

        @Override
        public CommandRuleBuilder dayLimit(long dayLimit) {
            commandRule.setDayLimit(dayLimit);
            return this;
        }

        @Override
        public CommandRuleBuilder weekLimit(long weekLimit) {
            commandRule.setWeekLimit(weekLimit);
            return this;
        }

        @Override
        public CommandRuleBuilder monthLimit(long monthLimit) {
            commandRule.setMonthLimit(monthLimit);
            return this;
        }

        @Override
        public CommandRuleBuilder command(String name) {
            Optional<DeviceMessageSpec> deviceMessageSpec = deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.valueOf(name).dbValue());
            commandRule.addCommand(deviceMessageSpec.get());
            return this;
        }

        @Override
        public CommandRule add() {
            this.commandRule.save();
            return commandRule;
        }
    }
}
