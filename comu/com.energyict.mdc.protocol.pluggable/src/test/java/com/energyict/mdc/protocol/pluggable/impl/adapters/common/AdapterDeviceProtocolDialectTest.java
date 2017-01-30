package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AdapterDeviceProtocolDialect} component.
 * <p>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 15:59
 */
@RunWith(MockitoJUnitRunner.class)
public class AdapterDeviceProtocolDialectTest {

    private static final String REQUIRED_PROPERTY_NAME = "RequiredProperty";
    private static final String OPTIONAL_PROPERTY_NAME = "OptionalProperty";

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LicenseService licenseService;
    @Mock
    private SearchService searchService;
    @Mock
    private Thesaurus thesaurus;

    private PropertySpecService propertySpecService;
    private DataModel dataModel;
    private InMemoryBootstrapModule bootstrapModule;

    @Before
    public void before() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.principal.getName()).thenReturn("AdapterDeviceProtocolDialectTest.mdc.protocol.pluggable");
        bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new NlsModule(),
                new EventsModule(),
                new IdsModule(),
                new UserModule(),
                new PartyModule(),
                new FiniteStateMachineModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new OrmModule(),
                new DataVaultModule(),
                new IssuesModule(),
                new PluggableModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolPluggableModule(),
                new CustomPropertySetsModule()
        );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(OrmService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(FiniteStateMachineService.class);
            this.propertySpecService = injector.getInstance(PropertySpecService.class);
            ProtocolPluggableService protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            this.dataModel = ((ProtocolPluggableServiceImpl) protocolPluggableService).getDataModel();
            ctx.commit();
        }
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        bootstrapModule.deactivate();
    }

    @Test
    public void testDialectName() {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol(propertySpecService);
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(thesaurus, mockDeviceProtocol);

        assertThat(dialect.getDeviceProtocolDialectName()).isEqualTo(MockMeterProtocol.class.getName());
    }

    @Test
    public void getRequiredKeysTest() {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol(propertySpecService);
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(thesaurus, mockDeviceProtocol);

        assertThat(getRequiredPropertiesFromSet(dialect.getPropertySpecs())).isEmpty();
    }

    @Test
    public void getOptionalKeysTest() {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol(propertySpecService);
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(thesaurus, mockDeviceProtocol);

        assertThat(getOptionalPropertiesFromSet(dialect.getPropertySpecs())).isEmpty();
    }

    @Test
    public void getPropertySpecTest() {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol(propertySpecService);
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(thesaurus, mockDeviceProtocol);

        assertThat(dialect.getPropertySpec(REQUIRED_PROPERTY_NAME)).isEmpty();
        assertThat(dialect.getPropertySpec(OPTIONAL_PROPERTY_NAME)).isEmpty();
    }

    private List<PropertySpec> getOptionalPropertiesFromSet(List<PropertySpec> propertySpecs) {
        return propertySpecs
                .stream()
                .filter(Predicates.not(PropertySpec::isRequired))
                .collect(Collectors.toList());
    }

    private List<PropertySpec> getRequiredPropertiesFromSet(List<PropertySpec> propertySpecs) {
        return propertySpecs
                .stream()
                .filter(PropertySpec::isRequired)
                .collect(Collectors.toList());
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(TimeService.class).toInstance(mock(TimeService.class));
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(SearchService.class).toProvider(() -> searchService);
        }
    }

}