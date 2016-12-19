package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.impl.DualControlModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.json.JsonService;

import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.impl.InMemoryIntegrationPersistence;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import static com.energyict.mdc.device.command.CommandRuleService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandRuleIT {

    protected static InMemoryIntegrationPersistence inMemoryPersistence;

    private static Injector injector;
    private static User principal;
    private static UserService userService;
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(transactionService);
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static CommandRuleService commandRuleService;
    private static TransactionService transactionService;
    private static DeviceMessageSpecificationService deviceMessageSpecificationService;
    private static DeviceMessageService deviceMessageService;
    private static DualControlService dualControlService;
    private static ThreadPrincipalService threadPrincipalService;
    private static JsonService jsonService;
    //THIS IS A MONDAY
    static final ZonedDateTime NOW = ZonedDateTime.of(2012, 10, 8, 1, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static AtomicLong offsets = new AtomicLong(1);
    static ProgrammableClock programmableClock = new ProgrammableClock(TimeZoneNeutral.getMcMurdo(), () -> NOW.plusSeconds(offsets.getAndIncrement()).toInstant());

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(DeviceMessageService.class).toInstance(mock(DeviceMessageService.class));
        }
    }

    public User getMockedUser() {
        return this.principal;
    }

    @BeforeClass
    public static void setUp() throws SQLException {

        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(programmableClock),
                    new DualControlModule(),
                    new TimeModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new ThreadSecurityModule(),
                    new NlsModule(),
                    new TaskModule(),
                    new EventsModule(),
                    new DataVaultModule(),
                    new UserModule(),
                    new MdcDynamicModule(),
                    new ProtocolApiModule(),
                    new BasicPropertiesModule(),
                    new CommandRuleModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            commandRuleService = injector.getInstance(CommandRuleService.class);
            deviceMessageSpecificationService = injector.getInstance(DeviceMessageSpecificationService.class);
            dualControlService = injector.getInstance(DualControlService.class);
            deviceMessageService = injector.getInstance(DeviceMessageService.class);
            threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
            userService = injector.getInstance(UserService.class);
            createUserAndChange();
            return null;
        });
        jsonService = injector.getInstance(JsonService.class);
    }

    private static void createUserAndChange() {
        principal = userService.createUser("TEST" + Instant.now(programmableClock).toEpochMilli(), "This user is just to satisfy the foreign key ...");
        principal.update();
        threadPrincipalService.set(principal);
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void createCommandLimitationRule() {
        CommandRule commandRule;
        commandRule = createRule("test", 1, 2, 3, 5);
        assertThat(commandRule).isNotNull();
        assertThat(commandRule.getName().equals("test"));
        assertThat(commandRule.getDayLimit()).isEqualTo(1);
        assertThat(commandRule.getWeekLimit()).isEqualTo(2);
        assertThat(commandRule.getMonthLimit()).isEqualTo(3);

        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(commandRule.getId());
        assertThat(reloadedRule).isPresent();
        commandRule = reloadedRule.get();

        assertThat(commandRule).isNotNull();
        assertThat(commandRule.getName().equals("test"));
        assertThat(commandRule.getDayLimit()).isEqualTo(1);
        assertThat(commandRule.getWeekLimit()).isEqualTo(2);
        assertThat(commandRule.getMonthLimit()).isEqualTo(3);
        assertThat(commandRule.getCommands()).hasSize(5);

    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_NAME + "}")
    public void createCommandLimitationRuleWithDuplicateName() {
        createRule("test", 10, 11, 12, 1);
        createRule("test", 10, 11, 12, 1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DAY_LIMIT_SMALLER_THAN_WEEK + "}", strict = false)
    public void createCommandLimitationRuleDayLimitBiggerThanWeek() {
        createRule("test", 11, 10, 12, 1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.WEEK_LIMIT_BIGGER_THAN_DAY + "}", strict = false)
    public void createCommandLimitationRuleWeekLimitSmallerThanDay() {
        createRule("test", 11, 10, 12, 1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DAY_LIMIT_SMALLER_THAN_WEEK_AND_MONTH + "}", strict = false)
    public void createCommandLimitationRuleDayLimitBiggerThanWeekAndMonth() {
        createRule("test", 11, 9, 8, 1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.WEEK_LIMIT_BIGGER_THAN_DAY_SMALLER_THAN_MONTH + "}", strict = false)
    public void createCommandLimitationRuleWeekLimitSmallerThanDayBiggerThanMonth() {
        createRule("test", 11, 9, 8, 1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MONTH_LIMIT_BIGGER_THAN_DAY_AND_WEEK + "}", strict = false)
    public void createCommandLimitationRuleMonthLimitSmallerThanWeekAndDay() {
        createRule("test", 11, 9, 8, 1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.AT_LEAST_ONE_COMMAND_REQUIRED + "}")
    public void createCommandLimitationRuleWithoutCommands() {
        createRule("test", 10, 11, 12, 0);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_COMMAND + "}")
    public void createCommandRuleDuplicateCommands() {
        commandRuleService.createRule("test")
                .dayLimit(1)
                .weekLimit(2)
                .monthLimit(3)
                .command(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE.name())
                .command(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE.name())
                .add();
    }

    @Test
    @Transactional
    public void tryActivate() {
        CommandRule testRule = createRule("test3", 10, 11, 12, 1);
        createUserAndChange();
        assertThat(testRule.isActive()).isFalse();
        testRule.activate();
        assertThat(testRule.isActive()).isFalse();
        assertThat(testRule.getCommandRulePendingUpdate().isPresent());

        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isPresent();
        testRule = reloadedRule.get();
        assertThat(testRule.isActive()).isFalse();
        assertThat(testRule.getCommandRulePendingUpdate().isPresent());
        assertThat(testRule.getCommandRulePendingUpdate().get().isActive());

        CommandRuleImpl commandRuleImpl = (CommandRuleImpl) testRule;
        createUserAndChange();
        commandRuleImpl.approve();
        createUserAndChange();
        commandRuleImpl.approve();
        assertThat(testRule.getCommandRulePendingUpdate()).isEmpty();

        reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isPresent();
        testRule = reloadedRule.get();
        assertThat(testRule.isActive()).isTrue();
        assertThat(testRule.getCommandRulePendingUpdate()).isEmpty();
    }

    @Test
    @Transactional
    public void tryDeactivate() {
        CommandRule testRule = createRule("test4", 10, 11, 12, 1);
        activateAndApproveRule(testRule);

        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isPresent();
        testRule = reloadedRule.get();
        assertThat(testRule.isActive()).isTrue();
        testRule.deactivate();
        createUserAndChange();
        testRule.approve();
        createUserAndChange();
        testRule.approve();

        assertThat(testRule.isActive()).isFalse();
        reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isPresent();
        testRule = reloadedRule.get();
        assertThat(testRule.isActive()).isFalse();
        assertThat(testRule.getCommandRulePendingUpdate()).isEmpty();

    }

    @Test
    @Transactional
    public void tryActivateReject() {
        CommandRule testRule = createRule("test4", 10, 11, 12, 1);
        CommandRule commandRuleImpl = testRule;
        testRule.activate();
        createUserAndChange();
        commandRuleImpl.approve();
        createUserAndChange();
        commandRuleImpl.reject();

        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isPresent();
        testRule = reloadedRule.get();
        assertThat(testRule.isActive()).isFalse();
        assertThat(testRule.getCommandRulePendingUpdate()).isEmpty();

    }

    @Test
    @Transactional
    public void testRemove() {
        CommandRule testRule = createRule("test4", 10, 11, 12, 1);
        commandRuleService.deleteRule(testRule);

        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isEmpty();
    }

    @Test
    @Transactional
    public void testRemoveDualControl() throws Exception {
        CommandRule testRule = createRule("test4", 10, 11, 12, 1);
        activateAndApproveRule(testRule);

        commandRuleService.deleteRule(testRule);
        testRule.approve();
        createUserAndChange();
        testRule.approve();

        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isEmpty();
    }

    private CommandRule createRule(String name, long dayLimit, long weekLimit, long monthLimit, long numberOfCommands) {
        createUserAndChange();
        CommandRuleBuilder builder = commandRuleService.createRule(name).dayLimit(dayLimit).weekLimit(weekLimit).monthLimit(monthLimit);
        for (int i = 0; i < numberOfCommands; i++) {
            builder.command(DeviceMessageId.values()[i].name());
        }
        return builder.add();
    }

    @Test
    @Transactional
    public void testEditInactive() {
        CommandRule testRule = createRule("test4", 10, 11, 12, 1);
        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isPresent();
        List<String> commandNames = Collections.singletonList(DeviceMessageId.values()[5].name());

        testRule.update("test2",5 , 8, 10, commandNames);
        reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isPresent();
        testRule = reloadedRule.get();
        assertThat(testRule.getName()).isEqualTo("test2");
        assertThat(testRule.getDayLimit()).isEqualTo(5);
        assertThat(testRule.getWeekLimit()).isEqualTo(8);
        assertThat(testRule.getMonthLimit()).isEqualTo(10);
        assertThat(testRule.getCommands().size()).isEqualTo(1);
        assertThat(testRule.getCommands().get(0).getCommand().getId().name()).isEqualTo(DeviceMessageId.values()[5].name());
    }

    @Test
    @Transactional
    public void testCreationOfCountersAndAllLimits() {
        DeviceMessageId deviceMessageId = DeviceMessageId.values()[1];
        CommandRule rule = createRule("test4", 1, 2, 3, 2);
        activateAndApproveRule(rule);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getDeviceMessageId()).thenReturn(deviceMessageId);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock));

        //checks if the command is valid to create and then creates it, creating the counters. Checks if the new limit is reached. (yes)
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isTrue();
        //checks if a command added the next day is valid (yes), create the command. Checks if the limit for the next day is reached (yes)
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock).plus(1, ChronoUnit.DAYS));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isTrue();
        //checks if a command added 2 days later is valid (no), create the command. Checks if the limit for the week is reached (yes)
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock).plus(2, ChronoUnit.DAYS));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isTrue();
        //checks if a command added a week later is valid (yes), create the command. Checks if the limit for the month is reached (yes)
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock).plus(7, ChronoUnit.DAYS));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock).plus(9, ChronoUnit.DAYS));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isTrue();
        //checks if a command added a month later is valid (yes)
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock).plus(30, ChronoUnit.DAYS));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();

    }

    @Test
    @Transactional
    public void testDayLimits() {
        DeviceMessageId deviceMessageId = DeviceMessageId.values()[0];
        CommandRule rule = createRule("test5", 2, 0, 0, 1);
        activateAndApproveRule(rule);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getDeviceMessageId()).thenReturn(deviceMessageId);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock));

        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isTrue();
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock).plus(1, ChronoUnit.DAYS));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
    }

    @Test
    @Transactional
    public void testWeekLimits() {
        DeviceMessageId deviceMessageId = DeviceMessageId.values()[0];
        CommandRule rule = createRule("test5", 0, 2, 0, 1);
        activateAndApproveRule(rule);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getDeviceMessageId()).thenReturn(deviceMessageId);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock));

        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isTrue();
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock).plus(7, ChronoUnit.DAYS));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
    }

    @Test
    @Transactional
    public void testMonthLimits() {
        DeviceMessageId deviceMessageId = DeviceMessageId.values()[0];
        CommandRule rule = createRule("test5", 0, 0, 2, 1);
        activateAndApproveRule(rule);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getDeviceMessageId()).thenReturn(deviceMessageId);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock));

        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isTrue();
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock).plus(30, ChronoUnit.DAYS));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
    }

    @Test
    @Transactional
    public void testUpdatedCommandLimits() {
        DeviceMessageId deviceMessageId = DeviceMessageId.values()[0];
        CommandRule rule = createRule("test5", 1, 0, 0, 1);
        activateAndApproveRule(rule);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        DeviceMessage deviceMessageReloaded = mock(DeviceMessage.class);
        when(deviceMessage.getDeviceMessageId()).thenReturn(deviceMessageId);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock));
        when(deviceMessageReloaded.getDeviceMessageId()).thenReturn(deviceMessageId);
        when(deviceMessageReloaded.getReleaseDate()).thenReturn(Instant.now(programmableClock));
        when(deviceMessageService.findDeviceMessageById(anyLong())).thenReturn(Optional.of(deviceMessageReloaded));
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isTrue();
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock).plus(1, ChronoUnit.DAYS));
        commandRuleService.commandUpdated(deviceMessage);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
    }

    @Test
    @Transactional
    public void testNoLimitsInactiveRule() {
        DeviceMessageId deviceMessageId = DeviceMessageId.values()[0];
        CommandRule rule = createRule("TEST", 1, 2, 3, 1);

        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getDeviceMessageId()).thenReturn(deviceMessageId);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock));
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
    }

    @Test
    @Transactional
    public void testDeletedCommandLimits() {
        DeviceMessageId deviceMessageId = DeviceMessageId.values()[0];
        CommandRule rule = createRule("test5", 1, 0, 0, 1);
        activateAndApproveRule(rule);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getDeviceMessageId()).thenReturn(deviceMessageId);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.now(programmableClock));
        commandRuleService.commandCreated(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isTrue();
        commandRuleService.commandDeleted(deviceMessage);
        assertThat(commandRuleService.limitsExceededForNewCommand(deviceMessage)).isFalse();
    }

    private void activateAndApproveRule(CommandRule rule) {
        rule.activate();
        createUserAndChange();
        rule.approve();
        createUserAndChange();
        rule.approve();
    }
}
