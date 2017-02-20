/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.MacException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.concurrent.DelayedRegistrationHandler;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.ICommandRuleCounter;
import com.energyict.mdc.device.command.ServerCommandRule;
import com.energyict.mdc.device.command.impl.exceptions.ExceededCommandRule;
import com.energyict.mdc.device.command.impl.exceptions.InvalidCommandRuleStatsException;
import com.energyict.mdc.device.command.security.Privileges;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private volatile Clock clock;
    private volatile DeviceMessageService deviceMessageService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile DataVaultService dataVaultService;

    private final DelayedRegistrationHandler delayedNotifications = new DelayedRegistrationHandler();

    public CommandRuleServiceImpl() {

    }

    @Inject
    CommandRuleServiceImpl(OrmService ormService, NlsService nlsService, BundleContext bundleContext, ThreadPrincipalService threadPrincipalService, DeviceMessageSpecificationService deviceMessageSpecificationService, UpgradeService upgradeService, UserService userService, DualControlService dualControlService, Clock clock, DeviceMessageService deviceMessageService, DataVaultService dataVaultService) {
        this();
        setThreadPrincipalService(threadPrincipalService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setUpgradeService(upgradeService);
        setUserService(userService);
        setDualControlService(dualControlService);
        setClock(clock);
        setDeviceMessageService(deviceMessageService);
        setDataVaultService(dataVaultService);
        activate(bundleContext);
    }

    @Override
    public List<CommandRule> findAllCommandRules() {
        checkCommandRuleStatsAndThrowException();
        return dataModel.mapper(CommandRule.class).find();
    }

    private List<CommandRule> findAllCommandRulesWithoutStatCheck() {
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
    public Optional<CommandRule> findAndLockCommandRule(long commandRuleId, long version) {
        return dataModel.mapper(CommandRule.class).lockObjectIfVersion(version, commandRuleId);
    }

    @Override
    public Optional<CommandRule> findCommandRuleByName(String name) {
        List<CommandRule> commandRules = dataModel.mapper(CommandRule.class).select(where(CommandRuleImpl.Fields.NAME.fieldName()).isEqualToIgnoreCase(name));
        return commandRules.isEmpty() ? Optional.empty() : Optional.of(commandRules.get(0));
    }

    @Override
    public Optional<CommandRulePendingUpdate> findCommandTemplateRuleByName(String name) {
        List<CommandRulePendingUpdate> commandRuleTemplates = dataModel.mapper(CommandRulePendingUpdate.class)
                .select(where(CommandRulePendingUpdateImpl.Fields.NAME.fieldName()).isEqualToIgnoreCase(name));
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
        return Stream.of(
                Stream.of(Privileges.values()),
                Stream.of(TranslationKeys.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Activate
    public void activate(BundleContext context) {
        CommandRuleService commandRuleService = this;
        LOGGER.info(() -> "Activating " + this.toString() + " from thread " + Thread.currentThread().getName());
        try {
            this.context = context;
            dataModel = ormService.newDataModel(CommandRuleService.COMPONENT_NAME, "MultiSense Command limitation rule");
            for (TableSpecs each : TableSpecs.values()) {
                each.addTo(dataModel, dataVaultService);
            }
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
                    bind(DeviceMessageService.class).toInstance(deviceMessageService);
                    bind(DataVaultService.class).toInstance(dataVaultService);
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

    @Reference(name = "ZZZOrmService")
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference(name = "AAANlsService")
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CommandRuleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference(name = "AAADeviceMessageSpecificationService")
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference(name = "AAAUpgradeService")
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference(name = "AAAUserService")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Reference(name = "AAADualControl")
    public void setDualControlService(DualControlService dualControlService) {
        this.dualControlService = dualControlService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Override
    public List<ExceededCommandRule> limitsExceededForUpdatedCommand(DeviceMessage deviceMessage, Instant oldReleaseDate) {
        return limitsExceededForCommand(deviceMessage, oldReleaseDate);
    }

    @Override
    public List<ExceededCommandRule> limitsExceededForNewCommand(DeviceMessage deviceMessage) {
        return limitsExceededForCommand(deviceMessage, null);

    }

    private List<ExceededCommandRule> limitsExceededForCommand(DeviceMessage deviceMessage, Instant oldReleaseDate) {
        List<CommandRule> commandRulesByDeviceMessageId = this.getActiveCommandRulesByDeviceMessageId(deviceMessage.getDeviceMessageId());
        if (commandRulesByDeviceMessageId.isEmpty()) {
            return Collections.emptyList();
        } else {
            return commandRulesByDeviceMessageId.stream()
                    .map(commandRule -> this.wouldCommandExceedLimits(commandRule, getCorrectReleaseDateForCalculations(deviceMessage.getReleaseDate()), oldReleaseDate))
                    .filter(ExceededCommandRule::isLimitExceeded)
                    .collect(Collectors.toList());
        }
    }

    private Instant getCorrectReleaseDateForCalculations(Instant releaseDate) {
        Range<Instant> dayOfNow = getDayFor(Instant.now(clock));
        if(releaseDate.isBefore(dayOfNow.lowerEndpoint())) {
            return dayOfNow.lowerEndpoint();
        } else {
            return releaseDate;
        }
    }

    private void checkCommandRuleStatsAndThrowException() {
        if (areCountersInValid()) {
            throw new InvalidCommandRuleStatsException(thesaurus, MessageSeeds.MAC_COMMAND_RULES_FAILED);
        }
    }

    void checkCommandRuleStats() {
        if (areCountersInValid()) {
            throw new MacException();
        }
    }

    private boolean areCountersInValid() {
        CommandRuleStats commandRuleStats = getCommandRuleStats();
        int numberOfCommandRules = findAllCommandRulesWithoutStatCheck().size();
        int numberOfCounters = dataModel.mapper(CommandRuleCounter.class).find().size();
        return commandRuleStats.getNrOfCounters() != numberOfCounters || commandRuleStats.getNrOfMessageRules() != numberOfCommandRules;
    }

    private List<CommandRule> getActiveCommandRulesByDeviceMessageId(DeviceMessageId deviceMessageId) {
        return findAllCommandRules().stream()
                .filter(ServerCommandRule::isActive)
                .filter(commandRule ->
                        commandRule.getCommands().stream()
                                .filter(commandInRule -> commandInRule.getCommand().getId().equals(deviceMessageId))
                                .findAny()
                                .isPresent()
                )
                .collect(Collectors.toList());
    }

    private ExceededCommandRule wouldCommandExceedLimits(CommandRule commandRule, Instant releaseDate, Instant oldReleaseDate) {
        ExceededCommandRule exceededCommandRule = new ExceededCommandRule(commandRule.getName());
        commandRule.getCounters()
                .stream()
                .map(CommandRuleCounter.class::cast)
                .filter(commandRuleCounter -> Range.closedOpen(commandRuleCounter.getFrom(), commandRuleCounter.getTo()).contains(releaseDate))
                .forEach(commandRuleCounter -> this.wouldExceedCounter(commandRuleCounter, oldReleaseDate, exceededCommandRule));

        return exceededCommandRule;
    }

    private ExceededCommandRule wouldExceedCounter(CommandRuleCounter commandRuleCounter, Instant oldReleaseDate, ExceededCommandRule exceededCommandRule) {
        CommandRule commandRule = commandRuleCounter.getCommandRule();
        long currentCount = commandRuleCounter.getCount();
        if (oldReleaseDate != null && Range.closedOpen(commandRuleCounter.getFrom(), commandRuleCounter.getTo()).contains(oldReleaseDate)) {
            currentCount--;
        }
        long limitToCheck;
        Consumer<Boolean> setter;
        switch (commandRuleCounter.getCounterType()) {
            case DAY:
                limitToCheck = commandRule.getDayLimit();
                setter = exceededCommandRule::setDayLimitExceeded;
                break;
            case WEEK:
                limitToCheck = commandRule.getWeekLimit();
                setter = exceededCommandRule::setWeekLimitExceeded;
                break;
            default:
                limitToCheck = commandRule.getMonthLimit();
                setter = exceededCommandRule::setMonthLimitExceeded;
                break;
        }
        boolean wouldExceedCounter = currentCount >= limitToCheck && limitToCheck != 0;
        if (wouldExceedCounter) {
            setter.accept(true);
            return exceededCommandRule;
        } else {
            return exceededCommandRule;
        }
    }

    @Override
    public void commandCreated(DeviceMessage deviceMessage) {
        increaseOrCreateCounters(deviceMessage, getCorrectReleaseDateForCalculations(deviceMessage.getReleaseDate()));
    }

    @Override
    public void commandUpdated(DeviceMessage deviceMessage, Instant oldReleaseDate) {
        decreaseExistingCounters(deviceMessage, oldReleaseDate);
        increaseOrCreateCounters(deviceMessage, getCorrectReleaseDateForCalculations(deviceMessage.getReleaseDate()));
    }

    @Override
    public void commandDeleted(DeviceMessage deviceMessage) {
        checkCommandRuleStatsAndThrowException();
        decreaseExistingCounters(deviceMessage, deviceMessage.getReleaseDate());
    }

    private void increaseOrCreateCounters(DeviceMessage deviceMessage, Instant releaseDate) {
        List<CommandRule> commandRulesByDeviceMessageId = this.getActiveCommandRulesByDeviceMessageId(deviceMessage.getDeviceMessageId());
        if (commandRulesByDeviceMessageId.isEmpty()) {
            return;
        }
        commandRulesByDeviceMessageId.stream()
                .forEach(commandRule -> increaseOrCreateCountersForRule(commandRule, releaseDate));
    }

    private void increaseOrCreateCountersForRule(CommandRule commandRule, Instant releaseDate) {
        List<CommandRuleCounter> applicableCounters = getApplicableCounters(commandRule, releaseDate);

        if (applicableCounters.size() > 3) {
            throw new IllegalArgumentException("Illegal situation: too many counters for given release date");
        } else {
            applicableCounters.forEach(CommandRuleCounter::increaseCount);
            createNewCounters(commandRule, releaseDate, applicableCounters, 1L).stream().forEach(commandRule::addCounter);
        }
        long numberOfCountersRemoved = ((CommandRuleImpl) commandRule).cleanUpCounters(getDayFor(Instant.now(clock)).lowerEndpoint());
        if (numberOfCountersRemoved > 0) {
            getCommandRuleStats().decreaseNumberOfCommandRuleCounters(numberOfCountersRemoved);
        }
    }

    @Override
    public List<ICommandRuleCounter> getCurrentCounters(CommandRule commandRule) {
        Instant now = Instant.now(clock);
        List<CommandRuleCounter> currentCounters = getApplicableCountersFor(commandRule.getCounters(), now).stream().collect(Collectors.toList());
        List<ICommandRuleCounter> counters = createNewCounters(commandRule, now, currentCounters, 0).stream().map(ICommandRuleCounter.class::cast).collect(Collectors.toList());
        counters.addAll(currentCounters);
        return counters;
    }

    private List<CommandRuleCounter> createNewCounters(CommandRule commandRule, Instant releaseDate, List<CommandRuleCounter> applicableCounters, long count) {
        boolean dayCounterExists = false;
        boolean weekCounterExists = false;
        boolean monthCounterExists = false;
        List<CommandRuleCounter> commandRuleCounters = new ArrayList<>();
        for (CommandRuleCounter counter : applicableCounters) {
            switch (counter.getCounterType()) {
                case DAY:
                    dayCounterExists = true;
                    break;
                case WEEK:
                    weekCounterExists = true;
                    break;
                default:
                    monthCounterExists = true;
                    break;
            }
        }
        if (!dayCounterExists && commandRule.getDayLimit() > 0) {
            commandRuleCounters.add(createCounter(getDayFor(releaseDate), commandRule, count));
        }
        if (!weekCounterExists && commandRule.getWeekLimit() > 0) {
            commandRuleCounters.add(createCounter(getWeekFor(releaseDate), commandRule, count));
        }
        if (!monthCounterExists && commandRule.getMonthLimit() > 0) {
            commandRuleCounters.add(createCounter(getMonthFor(releaseDate), commandRule, count));
        }

        return commandRuleCounters;
    }

    private CommandRuleCounter createCounter(Range<Instant> range, CommandRule commandRule, long count) {
        CommandRuleCounter counter = this.dataModel.getInstance(CommandRuleCounter.class);
        return counter.initialize(range.lowerEndpoint(), range.upperEndpoint(), count, commandRule);
    }

    private void decreaseExistingCounters(DeviceMessage deviceMessage, Instant oldReleaseDate) {
        List<CommandRule> commandRulesByDeviceMessageId = this.getActiveCommandRulesByDeviceMessageId(deviceMessage.getDeviceMessageId());
        if (commandRulesByDeviceMessageId.isEmpty()) {
            return;
        }
        commandRulesByDeviceMessageId.stream()
                .forEach(commandRule -> decreaseExistingCountersForRule(commandRule, oldReleaseDate));
    }

    private void decreaseExistingCountersForRule(CommandRule commandRule, Instant oldReleaseDate) {
        List<CommandRuleCounter> applicableCounters = getApplicableCounters(commandRule, oldReleaseDate);
        applicableCounters.forEach(CommandRuleCounter::decreaseCount);
    }

    private List<CommandRuleCounter> getApplicableCounters(CommandRule commandRule, Instant releaseDate) {
        return getApplicableCountersFor(commandRule.getCounters(), releaseDate);
    }

    private List<CommandRuleCounter> getApplicableCountersFor(List<ICommandRuleCounter> counters, Instant instant) {
        return counters.stream()
                .map(CommandRuleCounter.class::cast)
                .filter(commandRuleCounter -> Range.closedOpen(commandRuleCounter.getFrom(), commandRuleCounter.getTo()).contains(instant))
                .collect(Collectors.toList());
    }

    private Range<Instant> getDayFor(Instant instant) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, clock.getZone());
        ZonedDateTime startOfDay = zonedDateTime.toLocalDate().atStartOfDay(clock.getZone());
        ZonedDateTime endOfDay = startOfDay.plusDays(1);

        Instant start = startOfDay.toInstant();
        Instant end = endOfDay.toInstant();

        return Range.closedOpen(start, end);
    }

    private Range<Instant> getWeekFor(Instant instant) {
        ZonedDateTime releaseDateTime = ZonedDateTime.ofInstant(instant, clock.getZone());
        LocalDate mondayOfThisWeek = releaseDateTime.toLocalDate().with(ChronoField.DAY_OF_WEEK, 1);
        LocalDate mondayOfNextWeek = mondayOfThisWeek.plusDays(7);

        Instant start = ZonedDateTime.of(mondayOfThisWeek, LocalTime.MIDNIGHT, clock.getZone()).toInstant();
        Instant end = ZonedDateTime.of(mondayOfNextWeek, LocalTime.MIDNIGHT, clock.getZone()).toInstant();

        return Range.closedOpen(start, end);
    }

    private Range<Instant> getMonthFor(Instant instant) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, clock.getZone());
        LocalDate startOfMonth = zonedDateTime.withDayOfMonth(1).toLocalDate();
        LocalDate startOfNextMonth = startOfMonth.plusDays(startOfMonth.lengthOfMonth());

        Instant start = ZonedDateTime.of(startOfMonth, LocalTime.MIDNIGHT, clock.getZone()).toInstant();
        Instant end = ZonedDateTime.of(startOfNextMonth, LocalTime.MIDNIGHT, clock.getZone()).toInstant();

        return Range.closedOpen(start, end);
    }

    public void counterCreated() {
        getCommandRuleStats().increaseNumberOfCommandRuleCounters();
    }

    public void commandRuleCreated() {
        getCommandRuleStats().increaseNumberOfCommandRules();
    }

    public void commandRuleRemoved(long numberOfCounters) {
        getCommandRuleStats().decreaseNumberOfCommandRules();
        getCommandRuleStats().decreaseNumberOfCommandRuleCounters(numberOfCounters);
    }

    private CommandRuleStats getCommandRuleStats() {
        return dataModel.mapper(CommandRuleStats.class).select(where(CommandRuleStats.Fields.ID.fieldName()).isEqualTo(CommandRuleStats.ID)).get(0);
    }

    private class CommandRuleBuilderImpl implements CommandRuleBuilder {
        private CommandRuleImpl commandRule;

        public CommandRuleBuilderImpl(String name) {
            this.commandRule = new CommandRuleImpl(dataModel, deviceMessageSpecificationService, dualControlService, CommandRuleServiceImpl.this);
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
