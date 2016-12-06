package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.json.JsonService;

import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CommandRuleIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private CommandRuleService commandRuleService;
    private TransactionService transactionService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private JsonService jsonService;

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new TimeModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
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
            return null;
        });

        jsonService = injector.getInstance(JsonService.class);
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void createCommandLimitationRule() {
        CommandRule commandRule;
        try (TransactionContext context = transactionService.getContext()) {
            commandRule = commandRuleService.createRule("test").dayLimit(4).monthLimit(11).weekLimit(5).command(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE.name()).add();
            context.commit();
        }

        assertThat(commandRule).isNotNull();
        assertThat(commandRule.getName().equals("test"));
        assertThat(commandRule.getDayLimit()).isEqualTo(4);
        assertThat(commandRule.getWeekLimit()).isEqualTo(5);
        assertThat(commandRule.getMonthLimit()).isEqualTo(11);


        Optional<CommandRule> reloadedRule = commandRuleService.findCommandRule(commandRule.getId());
        assertThat(reloadedRule).isPresent();
        commandRule = reloadedRule.get();

        assertThat(commandRule).isNotNull();
        assertThat(commandRule.getName().equals("test"));
        assertThat(commandRule.getDayLimit()).isEqualTo(4);
        assertThat(commandRule.getWeekLimit()).isEqualTo(5);
        assertThat(commandRule.getMonthLimit()).isEqualTo(11);

    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_NAME + "}")
    public void createCommandLimitationRuleWithDuplicateName() {
        try (TransactionContext context = transactionService.getContext()) {
            commandRuleService.createRule("test").dayLimit(10).weekLimit(11).monthLimit(12).command(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE.name()).add();
            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            commandRuleService.createRule("test").command(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE.name()).add();
            context.commit();
        }

    }

}
