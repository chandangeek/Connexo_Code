/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.EventType;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.security.Principal;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that the appropriate event is published when
 * properties are added/removed from a PartialConnectionTask.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-15 (10:23)
 */
@RunWith(MockitoJUnitRunner.class)
public class AddRemovePartialConnectionTaskPropertyTest {

    private static final String HOST_PROPERTY_SPEC_NAME = "host";
    private static final String PORT_PROPERTY_SPEC_NAME = "port";
    private static final String TIMEOUT_PROPERTY_SPEC_NAME = "timeout";
    private static final long FIXED_ID = 97L;

    private static InMemoryBootstrapModule bootstrapModule;
    private static EventAdmin eventAdmin;
    private static BundleContext bundleContext;
    private static LicenseService licenseService;
    private static TransactionService transactionService;
    private static EventService eventService;
    private static Publisher publisher;
    private static NlsService nlsService;
    private static Thesaurus thesaurus;
    private static ValidationService validationService;
    private static EstimationService estimationService;
    private static MeteringService meteringService;
    private static UserService userService;
    private static JsonService jsonService;
    private static BeanService beanService = new BeanServiceImpl();
    private static Clock clock = Clock.systemDefaultZone();
    private static FileSystem fileSystem = FileSystems.getDefault();

    @Rule
    public final TestRule transactional = new TransactionalRule(transactionService);

    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private ConnectionTypePluggableClass connectionTypePluggableClass;
    @Mock
    private ConnectionType connectionType;
    @Mock
    private SchedulingService schedulingService;
    @Mock
    private DataModel dataModel;
    @Mock
    private MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceConfiguration deviceConfiguration;

    @BeforeClass
    public static void initializeDatabase() {
        initializeStaticMocks();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(AddRemovePartialConnectionTaskPropertyTest.class.getSimpleName());
        Injector injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new TransactionModule(false),
                new H2OrmModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(OrmService.class);
            eventService = injector.getInstance(EventService.class);
            installEventTypes();
            ctx.commit();
        }
    }

    @AfterClass
    public static void cleanUpDataBase() {
        bootstrapModule.deactivate();
    }

    private static void installEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

    private static void initializeStaticMocks() {
        eventAdmin = mock(EventAdmin.class);
        bundleContext = mock(BundleContext.class);
        licenseService = mock(LicenseService.class);
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        bootstrapModule = new InMemoryBootstrapModule();
        thesaurus = mock(Thesaurus.class);
        nlsService = mock(NlsService.class);
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        publisher = mock(Publisher.class);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });
        validationService = mock(ValidationService.class);
        estimationService = mock(EstimationService.class);
        meteringService = mock(MeteringService.class);
        userService = mock(UserService.class);
        jsonService = mock(JsonService.class);
        initializeJsonService();
    }

    private static void initializeJsonService() {
        when(jsonService.serialize(anyObject())).thenReturn("All objects are serialized equally");
    }

    private static void resetAndInitializeJsonService() {
        reset(jsonService);
        initializeJsonService();
    }

    @Before
    public void initializeMocks() throws SQLException {
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
        this.initializeConnectionTypeMocks();
        this.initializeDataModel();
    }

    private void initializeConnectionTypeMocks() {
        PropertySpec hostPropertySpec = mock(PropertySpec.class);
        when(hostPropertySpec.getName()).thenReturn(HOST_PROPERTY_SPEC_NAME);
        when(hostPropertySpec.isRequired()).thenReturn(true);
        when(hostPropertySpec.getValueFactory()).thenReturn(new StringFactory());
        PropertySpec portPropertySpec = mock(PropertySpec.class);
        when(portPropertySpec.getName()).thenReturn(PORT_PROPERTY_SPEC_NAME);
        when(portPropertySpec.isRequired()).thenReturn(true);
        when(portPropertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        PropertySpec timeOutPropertySpec = mock(PropertySpec.class);
        when(timeOutPropertySpec.getName()).thenReturn(TIMEOUT_PROPERTY_SPEC_NAME);
        when(timeOutPropertySpec.isRequired()).thenReturn(false);
        when(timeOutPropertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        when(this.connectionType.getPropertySpecs()).thenReturn(Arrays.asList(hostPropertySpec, portPropertySpec, timeOutPropertySpec));
        when(this.connectionType.getPropertySpec(HOST_PROPERTY_SPEC_NAME)).thenReturn(Optional.of(hostPropertySpec));
        when(this.connectionType.getPropertySpec(PORT_PROPERTY_SPEC_NAME)).thenReturn(Optional.of(portPropertySpec));
        when(this.connectionType.getPropertySpec(TIMEOUT_PROPERTY_SPEC_NAME)).thenReturn(Optional.of(timeOutPropertySpec));
        when(this.connectionTypePluggableClass.getConnectionType()).thenReturn(this.connectionType);
        when(this.connectionTypePluggableClass.getPropertySpecs()).thenReturn(Arrays.asList(hostPropertySpec, portPropertySpec, timeOutPropertySpec));
        when(this.connectionTypePluggableClass.getPropertySpec(HOST_PROPERTY_SPEC_NAME)).thenReturn(Optional.of(hostPropertySpec));
        when(this.connectionTypePluggableClass.getPropertySpec(PORT_PROPERTY_SPEC_NAME)).thenReturn(Optional.of(portPropertySpec));
        when(this.connectionTypePluggableClass.getPropertySpec(TIMEOUT_PROPERTY_SPEC_NAME)).thenReturn(Optional.of(timeOutPropertySpec));
    }

    private void initializeDataModel() {
        Validator validator = mock(Validator.class);
        when(validator.validate(anyObject(), anyVararg())).thenReturn(Collections.emptySet());
        ValidatorFactory validatorFactory = mock(ValidatorFactory.class);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(this.dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(this.dataModel.getInstance(PartialScheduledConnectionTaskImpl.class)).thenReturn(this.newInstanceFromDataModel());
    }

    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testAddRequiredProperty() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.valueOf(4059L));
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "localhost");
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).contains(HOST_PROPERTY_SPEC_NAME);
    }

    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testRemoveRequiredProperty() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "localhost");
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.valueOf(4059L));
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.removeProperty(HOST_PROPERTY_SPEC_NAME);
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).contains(HOST_PROPERTY_SPEC_NAME);
    }

    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testAddAllRequiredProperties() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "localhost");
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.valueOf(4059L));
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).contains(HOST_PROPERTY_SPEC_NAME, PORT_PROPERTY_SPEC_NAME);
    }

    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testRemoveAllRequiredProperties() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "localhost");
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.valueOf(4059L));
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.removeProperty(HOST_PROPERTY_SPEC_NAME);
        partialConnectionTask.removeProperty(PORT_PROPERTY_SPEC_NAME);
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).contains(HOST_PROPERTY_SPEC_NAME, PORT_PROPERTY_SPEC_NAME);
    }

    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testRemoveAndSetAgainForRequiredProperties() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "localhost");
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.valueOf(4059L));
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.removeProperty(HOST_PROPERTY_SPEC_NAME);
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "some.host");
        partialConnectionTask.removeProperty(PORT_PROPERTY_SPEC_NAME);
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.TEN);
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).isEmpty();
    }
    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testSetAndRemoveAgainForRequiredProperties() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "some.host");
        partialConnectionTask.removeProperty(HOST_PROPERTY_SPEC_NAME);
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.TEN);
        partialConnectionTask.removeProperty(PORT_PROPERTY_SPEC_NAME);
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).isEmpty();
    }
    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testAddOptionalProperty() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "localhost");
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.valueOf(4059L));
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.setProperty(TIMEOUT_PROPERTY_SPEC_NAME, BigDecimal.ONE);
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).isEmpty();
    }
    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testRemoveOptionalProperty() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "localhost");
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.valueOf(4059L));
        partialConnectionTask.setProperty(TIMEOUT_PROPERTY_SPEC_NAME, BigDecimal.ONE);
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.removeProperty(TIMEOUT_PROPERTY_SPEC_NAME);
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).isEmpty();
    }
    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testSetAndRemoveAgainForOptionalProperty() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "localhost");
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.valueOf(4059L));
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.setProperty(TIMEOUT_PROPERTY_SPEC_NAME, BigDecimal.TEN);
        partialConnectionTask.removeProperty(TIMEOUT_PROPERTY_SPEC_NAME);
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).isEmpty();
    }
    @Ignore("FIXME: CXO-12404")
    @Test
    @Transactional
    public void testRemoveAndSetAgainForOptionalProperty() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = this.testInstance();
        partialConnectionTask.setProperty(HOST_PROPERTY_SPEC_NAME, "localhost");
        partialConnectionTask.setProperty(PORT_PROPERTY_SPEC_NAME, BigDecimal.valueOf(4059L));
        partialConnectionTask.setProperty(TIMEOUT_PROPERTY_SPEC_NAME, BigDecimal.ONE);
        partialConnectionTask.save();
        resetAndInitializeJsonService();
        reset(publisher);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {
        });

        // Business method
        partialConnectionTask.removeProperty(TIMEOUT_PROPERTY_SPEC_NAME);
        partialConnectionTask.setProperty(TIMEOUT_PROPERTY_SPEC_NAME, BigDecimal.TEN);
        partialConnectionTask.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(PartialConnectionTaskUpdateDetails.class);
        assertThat(((PartialConnectionTaskUpdateDetails) localEvent.getSource()).getAddedOrRemovedRequiredProperties()).isEmpty();
    }

    private PartialScheduledConnectionTaskImpl testInstance() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = PartialScheduledConnectionTaskImpl.from(this.dataModel, this.deviceConfiguration);
        partialConnectionTask.setConnectionTypePluggableClass(this.connectionTypePluggableClass);
        return partialConnectionTask;
    }

    private PartialScheduledConnectionTaskImpl newInstanceFromDataModel() {
        PartialScheduledConnectionTaskImpl partialConnectionTask = new PartialScheduledConnectionTaskImpl(this.dataModel, eventService, thesaurus, this.protocolPluggableService, this.schedulingService);
        partialConnectionTask.setId(FIXED_ID);
        return partialConnectionTask;
    }

    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }

    private static class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(NlsService.class).toInstance(nlsService);
            bind(ValidationService.class).toInstance(validationService);
            bind(EstimationService.class).toInstance(estimationService);
            bind(MeteringService.class).toInstance(meteringService);
            bind(Publisher.class).toInstance(publisher);
            bind(UserService.class).toInstance(userService);
            bind(JsonService.class).toInstance(jsonService);
            bind(BeanService.class).toInstance(beanService);
            bind(Clock.class).toInstance(clock);
            bind(FileSystem.class).toInstance(fileSystem);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

}