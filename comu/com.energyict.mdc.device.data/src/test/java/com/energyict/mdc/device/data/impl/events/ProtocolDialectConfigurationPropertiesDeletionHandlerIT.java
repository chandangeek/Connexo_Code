package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.impl.DeviceConfigurationImpl;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.InMemoryIntegrationPersistence;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.TimeZone;

import org.junit.*;
import org.junit.rules.*;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Integration test for the {@link ProtocolDialectConfigurationPropertiesDeletionHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-19 (09:00)
 */
public class ProtocolDialectConfigurationPropertiesDeletionHandlerIT {

    private static final String DIALECT_NAME = TestProtocolWithRequiredStringAndOptionalNumericDialectProperties.DIALECT_NAME;
    private static final String STRING_PROPERTY_NAME = TestProtocolWithRequiredStringAndOptionalNumericDialectProperties.STRING_PROPERTY_NAME;
    private static final String DEVICENAME = "deviceName";
    private static final String MRID = "MyUniqueMRID";
    private static final String DEVICE_TYPE_NAME = "ProtDialConfPropsDelHandlerIT";
    private static final String DEVICE_CONFIGURATION_NAME = "ProtDialConfPropsDelHandlerITConfig";
    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

    private static DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private static InMemoryIntegrationPersistence inMemoryPersistence;
    private static ProtocolDialectConfigurationPropertiesDeletionHandler topicHandler;
    private DeviceConfiguration deviceConfiguration;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;

    @BeforeClass
    public static void initialize() throws SQLException {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        initializeClock();
        inMemoryPersistence.initializeDatabase("ProtocolDialectConfigurationPropertiesDeletionHandlerIT", false);
        createDeviceProtocol();
        addEventHandlers();
    }

    private static void addEventHandlers() {
        topicHandler = new ProtocolDialectConfigurationPropertiesDeletionHandler(inMemoryPersistence.getDeviceDataModelService());
        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(topicHandler);
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    private static void initializeClock() {
        when(inMemoryPersistence.getClock().getZone()).thenReturn(utcTimeZone.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    public static void createDeviceProtocol() {
        try (TransactionContext context = getTransactionService().getContext()) {
            deviceProtocolPluggableClass =
                    inMemoryPersistence.getProtocolPluggableService()
                            .newDeviceProtocolPluggableClass("Pluggable", TestProtocolWithRequiredStringAndOptionalNumericDialectProperties.class.getName());
            deviceProtocolPluggableClass.save();
            context.commit();
        }
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @AfterClass
    public static void removeTopicHandlers() {
        if (topicHandler != null) {
            ((EventServiceImpl) inMemoryPersistence.getEventService()).removeTopicHandler(topicHandler);
        }
    }

    @Before
    public void initializeMocks() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        deviceType.save();
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfigurationBuilder.isDirectlyAddressable(true);
        this.deviceConfiguration = deviceConfigurationBuilder.add();
        this.deviceConfiguration.activate();
        this.initializeDialectProperties();
    }

    public void initializeDialectProperties() {
        DeviceProtocolDialect deviceProtocolDialect = new TestProtocolWithRequiredStringAndOptionalNumericDialectProperties().getDeviceProtocolDialects().get(0);
        ProtocolDialectConfigurationProperties configurationProperties = this.deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(deviceProtocolDialect);
        configurationProperties.setProperty(STRING_PROPERTY_NAME, "Configuration value");
        configurationProperties.save();
    }

    @After
    public void resetClock () {
        initializeClock();
    }

    @Test
    @Transactional
    public void deleteWhenNotUsed() {
        List<ProtocolDialectConfigurationProperties> protocolDialectConfigurationPropertiesList = this.deviceConfiguration.getProtocolDialectConfigurationPropertiesList();
        int numberOfConfigurationPropertiesBeforeDelete = protocolDialectConfigurationPropertiesList.size();
        DeviceProtocolDialect deviceProtocolDialect = new TestProtocolWithRequiredStringAndOptionalNumericDialectProperties().getDeviceProtocolDialects().get(0);

        // Business method
        ((DeviceConfigurationImpl) this.deviceConfiguration).removeProtocolDialectConfigurationProperties(deviceProtocolDialect);

        // Asserts
        int numberOfConfigurationPropertiesAfterDelete = this.deviceConfiguration.getProtocolDialectConfigurationPropertiesList().size();
        assertThat(numberOfConfigurationPropertiesAfterDelete).isLessThan(numberOfConfigurationPropertiesBeforeDelete);
    }

    @Test(expected = VetoDeleteProtocolDialectConfigurationPropertiesException.class)
    @Transactional
    public void deleteWhenUsed() {
        Device device = this.createDevice();
        device.setProtocolDialectProperty(DIALECT_NAME, STRING_PROPERTY_NAME, "device value");
        device.save();
        DeviceProtocolDialect deviceProtocolDialect = new TestProtocolWithRequiredStringAndOptionalNumericDialectProperties().getDeviceProtocolDialects().get(0);

        // Business method
        ((DeviceConfigurationImpl) this.deviceConfiguration).removeProtocolDialectConfigurationProperties(deviceProtocolDialect);

        // Asserts: see expected exception rule
    }

    private Device createDevice() {
        return createDeviceWithName(DEVICENAME, MRID);
    }

    private Device createDeviceWithName(String name, String mRID){
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

}