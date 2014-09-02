package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.users.impl.UserModule;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Tests the {@link AdapterDeviceProtocolDialect} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 15:59
 */
@RunWith(MockitoJUnitRunner.class)
public class AdapterDeviceProtocolDialectTest {

    private static final String REQUIRED_PROPERTY_NAME = "RequiredProperty";
    private static final String OPTIONAL_PROPERTY_NAME = "OptionalProperty";
    private static final String FIRST_ADDITIONAL_PROPERTY_NAME = "FirstAdditionalProperty";

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LicenseService licenseService;

    private PropertySpecService propertySpecService;
    private ProtocolPluggableService protocolPluggableService;
    private DataModel dataModel;
    private InMemoryBootstrapModule bootstrapModule;

    @Before
    public void before () {
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
                new UserModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new IssuesModule(),
                new PluggableModule(),
                new MdcCommonModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolsModule(),
                new ProtocolPluggableModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(OrmService.class);
            this.propertySpecService = injector.getInstance(PropertySpecService.class);
            this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            this.dataModel = ((ProtocolPluggableServiceImpl) protocolPluggableService).getDataModel();
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        environment.setApplicationContext(applicationContext);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        bootstrapModule.deactivate();
    }

    @Test
    public void testDialectName () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(propertySpecService, protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(dialect.getDeviceProtocolDialectName()).isEqualTo("MockMeterPro316065908");
    }

    @Test
    public void getRequiredKeysTest () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(propertySpecService, protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(getRequiredPropertiesFromSet(dialect.getPropertySpecs())).containsOnly(getRequiredPropertySpec());
    }

    @Test
    public void getOptionalKeysTest () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(propertySpecService, protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(getOptionalPropertiesFromSet(dialect.getPropertySpecs())).containsOnly(getOptionalPropertySpec());
    }

    @Test
    public void getPropertySpecTest () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(propertySpecService, protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(dialect.getPropertySpec(REQUIRED_PROPERTY_NAME)).isEqualTo(getRequiredPropertySpec());
        assertThat(dialect.getPropertySpec(OPTIONAL_PROPERTY_NAME)).isEqualTo(getOptionalPropertySpec());
    }

    @Test
    public void testWithAdditionalProperties () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(propertySpecService, protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(dialect.getPropertySpecs()).containsOnly(this.getPropertySpecs());
    }

    @Test
    public void testWithRemovableProperties () {
        MockMeterProtocol mockDeviceProtocol = new MockMeterProtocol();
        List<PropertySpec> removableProperties = Arrays.asList(getFirstRemovableProperty(), getSecondRemovableProperty());
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(propertySpecService, protocolPluggableService, mockDeviceProtocol, removableProperties);

        assertThat(dialect.getPropertySpecs()).containsOnly(this.getOptionalPropertySpec());
    }

    @Test
    public void testWithOnlyAdditionalProperties () {
        MeterProtocol mockDeviceProtocol = mock(MeterProtocol.class);
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(propertySpecService, protocolPluggableService, mockDeviceProtocol, new ArrayList<PropertySpec>());

        assertThat(dialect.getPropertySpecs()).isEmpty();
    }

    @Test
    public void testWithOnlyRemovableProperties () {
        MeterProtocol mockDeviceProtocol = mock(MeterProtocol.class);
        List<PropertySpec> removableProperties = Arrays.asList(getFirstRemovableProperty(), getSecondRemovableProperty());
        AdapterDeviceProtocolDialect dialect = new AdapterDeviceProtocolDialect(propertySpecService, protocolPluggableService, mockDeviceProtocol, removableProperties);

        assertThat(dialect.getPropertySpecs()).isEmpty();
    }

    private List<PropertySpec> getOptionalPropertiesFromSet(List<PropertySpec> propertySpecs) {
        List<PropertySpec> requiredProperties = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            if(!propertySpec.isRequired()){
                requiredProperties.add(propertySpec);
            }
        }
        return requiredProperties;
    }

    private List<PropertySpec> getRequiredPropertiesFromSet(List<PropertySpec> propertySpecs) {
        List<PropertySpec> requiredProperties = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            if(propertySpec.isRequired()){
                requiredProperties.add(propertySpec);
            }
        }
        return requiredProperties;
    }

    private PropertySpec<String>[] getPropertySpecs () {
        PropertySpec<String>[] allPropertySpecs = new PropertySpec[2];
        allPropertySpecs[0] = this.getRequiredPropertySpec();
        allPropertySpecs[1] = this.getOptionalPropertySpec();
        return allPropertySpecs;
    }

    private PropertySpec<String> getRequiredPropertySpec () {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(REQUIRED_PROPERTY_NAME);
    }

    private PropertySpec<String> getOptionalPropertySpec () {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(OPTIONAL_PROPERTY_NAME);
    }

    private PropertySpec getFirstRemovableProperty () {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(REQUIRED_PROPERTY_NAME);
    }

    private PropertySpec getSecondRemovableProperty () {
        return OptionalPropertySpecFactory.newInstance().stringPropertySpec(FIRST_ADDITIONAL_PROPERTY_NAME);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }
    }
}