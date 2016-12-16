package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
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
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserImpl;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.json.JsonService;

import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.impl.InMemoryIntegrationPersistence;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.Message;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.swing.text.html.Option;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


import static com.energyict.mdc.device.command.CommandRuleService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    private static DualControlService dualControlService;
    private static ThreadPrincipalService threadPrincipalService;
    private static JsonService jsonService;

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
                    new UtilModule(),
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
            threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
            userService = injector.getInstance(UserService.class);
            createUserAndChange(1);
            return null;
        });
        jsonService = injector.getInstance(JsonService.class);
    }

    private static void createUserAndChange(long id) {
        principal = userService.createUser("TEST" + id, "This user is just to satisfy the foreign key ...");
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
        createUserAndChange(8);
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
        createUserAndChange(7);
        commandRuleImpl.approve();
        createUserAndChange(2);
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
        createUserAndChange(9);
        CommandRule testRule = createRule("test4", 10, 11, 12, 1);
        CommandRuleImpl commandRuleImpl = (CommandRuleImpl) testRule;
        testRule.activate();
        createUserAndChange(3);
        commandRuleImpl.approve();
        createUserAndChange(4);
        commandRuleImpl.approve();

        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isPresent();
        testRule = reloadedRule.get();
        assertThat(testRule.isActive()).isTrue();
        testRule.deactivate();
        createUserAndChange(5);
        commandRuleImpl = (CommandRuleImpl) testRule;
        commandRuleImpl.approve();
        createUserAndChange(6);
        commandRuleImpl.approve();

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
        createUserAndChange(10);
        CommandRule testRule = createRule("test4", 10, 11, 12, 1);
        CommandRuleImpl commandRuleImpl = (CommandRuleImpl) testRule;
        testRule.activate();
        createUserAndChange(11);
        commandRuleImpl.approve();
        createUserAndChange(12);
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
        createUserAndChange(13);
        CommandRuleImpl testRule = (CommandRuleImpl) createRule("test4", 10, 11, 12, 1);
        testRule.save();
        testRule.activate();
        testRule.approve();
        createUserAndChange(14);
        testRule.approve();

        testRule.delete();
        testRule.approve();
        createUserAndChange(15);
        testRule.approve();

        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(testRule.getId());
        assertThat(reloadedRule).isEmpty();
    }

    private CommandRule createRule(String name, long dayLimit, long weekLimit, long monthLimit, long numberOfCommands) {
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
}
