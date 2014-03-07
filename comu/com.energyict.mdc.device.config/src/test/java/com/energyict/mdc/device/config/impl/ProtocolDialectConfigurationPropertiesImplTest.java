package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.ValueFactory;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the peristence aspects of the {@link com.energyict.mdc.device.config.impl.RegisterMappingImpl} component
 * as provided by the {@link com.energyict.mdc.device.config.impl.DeviceConfigurationServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectConfigurationPropertiesImplTest extends PersistenceTest {


    static final String DEVICE_TYPE_NAME = PersistenceTest.class.getName() + "Type";
    private static final String NAME = "name";
    private static final String MY_PROPERTY = "myProperty";
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceCommunicationConfiguration configuration;
    @Mock
    private DeviceConfiguration deviceconfiguration;
    @Mock
    private DeviceProtocolDialect protocolDialect;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private ValueFactory valueFactory;
    @Mock
    private DeviceType myDeviceType;

    @Before
    public void setUp() {
        when(configuration.getDeviceConfiguration()).thenReturn(deviceconfiguration);
        when(deviceconfiguration.getDeviceType()).thenReturn(myDeviceType);
        when(protocolDialect.getDeviceProtocolDialectName()).thenReturn("protocolDialect");
        when(protocolDialect.getPropertySpec(MY_PROPERTY)).thenReturn(propertySpec);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(valueFactory.fromStringValue("15")).thenReturn(15);
        when(valueFactory.toStringValue(15)).thenReturn("15");
//        when(deviceconfiguration.get)
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateWithoutViolations() {
        Injector injector = inMemoryPersistence.getInjector();
        DataModel dataModel = injector.getInstance(DataModel.class);
        long id = 0;

        try (TransactionContext context = getTransactionService().getContext()) {
            ProtocolDialectConfigurationProperties properties = ProtocolDialectConfigurationPropertiesImpl.from(dataModel, configuration, NAME, protocolDialect);

            properties.setProperty(MY_PROPERTY, 15);

            properties.save();

            id = properties.getId();

            context.commit();
        }

        ProtocolDialectConfigurationProperties existing = dataModel.mapper(ProtocolDialectConfigurationProperties.class).getExisting(id);

        assertThat(existing.getProperty("myProperty")).isEqualTo(15);

    }


}