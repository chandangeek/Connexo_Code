package com.energyict.mdc.tasks;

import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;

import java.util.List;

import org.junit.*;
import org.junit.rules.*;

import static org.mockito.Mockito.mock;

public class PersistenceTest {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void staticSetUp() {
        BundleContext bundleContext = mock(BundleContext.class);

        injector = Guice.createInjector(
                new MockModule(bundleContext),
                inMemoryBootstrapModule,
                new OrmModule(),
                new UtilModule(),
                new NlsModule(),
                new EventsModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new InMemoryMessagingModule(),
                new IssuesModule(),
                new DataVaultModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new MdcReadingTypeUtilServiceModule(),
                new UserModule(),
                new PartyModule(),
                new IdsModule(),
                new DomainUtilModule(),
                new FiniteStateMachineModule(),
                new MeteringModule("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                new MasterDataModule(),
                new PluggableModule(),
                new ProtocolApiModule(),
                new TransactionModule(false),
                new TasksModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(NlsService.class); // fake call to make sure component is initialized
            injector.getInstance(EventService.class); // fake call to make sure component is initialized
            injector.getInstance(FiniteStateMachineService.class); // fake call to make sure component is initialized
            injector.getInstance(MasterDataService.class); // fake call to make sure component is initialized
            injector.getInstance(TaskService.class); // fake call to make sure component is initialized
            deviceMessageSpecificationService = injector.getInstance(DeviceMessageSpecificationService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void staticTearDown() {
    	inMemoryBootstrapModule.deactivate();
    }

    public static TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    public final TaskService getTaskService() {
        return injector.getInstance(TaskService.class);
    }

    public final MasterDataService getMasterDataService() {
        return injector.getInstance(MasterDataService.class);
    }

    public final MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    public final DeviceMessageSpecificationService getDeviceMessageService() {
        return deviceMessageSpecificationService;
    }

    public final DataModel getDataModel(){
        return injector.getInstance(OrmService.class).getDataModel(TaskService.COMPONENT_NAME).get();
    }

    protected <T extends ProtocolTask> T getTaskByType(List<? extends ProtocolTask> protocolTasks, Class<T> clazz) {
        for (ProtocolTask protocolTask : protocolTasks) {
            if (clazz.isAssignableFrom(protocolTask.getClass())) {
                return (T) protocolTask;
            }
        }
        return null;
    }


}
