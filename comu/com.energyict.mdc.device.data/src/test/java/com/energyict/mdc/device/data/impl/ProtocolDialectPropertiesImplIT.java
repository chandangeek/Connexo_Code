package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceUsageType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.exceptions.ProtocolDialectConfigurationPropertiesIsRequiredException;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectProperty;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Strings;
import com.google.common.collect.Range;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for the {@link ProtocolDialectPropertiesImpl} component.
 *
 * Copyrights EnergyICT
 * Date: 26/04/13
 * Time: 16:46
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectPropertiesImplIT extends PersistenceIntegrationTest {

    private static final String REQUIRED_PROPERTY_NAME_D1 = "ThisIsTheRequiredPropertyName";
    private static final String REQUIRED_PROPERTY_VALUE = "lmskdjfsmldkfjsqlmdkfj";
    private static final String OPTIONAL_PROPERTY_NAME_D1 = "ThisIsTheOptionalPropertyName";
    private static final String OPTIONAL_PROPERTY_VALUE = "sdlfkjnsqdlmfjsqdfsqdfsqdf";
    private static final String OPTIONAL_PROPERTY_WITH_LONG_NAME_D1 = "OptionalPropertyWithExtremelyLongNameWhichIsGoingToBeConverted";
    private static final String OPTIONAL_PROPERTY_WITH_CONVERTED_NAME = "OptionalPropertyWith1227106396";
    private static final String OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE = "jklmdsqfjkldsqlozidkcxjnnclsqkdkjoijfze65465zef65e6f51ze6f51zefze";
    private static final String INHERITED_OPTIONAL_PROPERTY_VALUE = "inheritedmqjdsflmdsqkjflmsqdjkfmsqldkfjlmdsqjkf";
    private static final String REQUIRED_PROPERTY_NAME_D2 = "RequiredDialect2";
    private static final String OPTIONAL_PROPERTY_NAME_D2 = "OptionalDialect2";

    private static final String DIALECT_1_NAME = TestProtocolDialect1.class.getSimpleName();
    private static final String DIALECT_2_NAME = TestProtocolDialect2.class.getSimpleName();

    private static final String MRID = "mRID";

    private static DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private static DeviceType deviceType;
    private static DeviceConfiguration deviceConfiguration;
    private static ProtocolDialectConfigurationProperties protocolDialect1ConfigurationProperties;

    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceProtocolDialect deviceProtocolDialect;

    @BeforeClass
    public static void setupDeviceProtocolAndDeviceConfiguration() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                registerDeviceProtocol();
                deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(ProtocolDialectPropertiesImplIT.class.getSimpleName(), deviceProtocolPluggableClass);
                deviceType.setDeviceUsageType(DeviceUsageType.METER);
                DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(ProtocolDialectPropertiesImplIT.class.getName());
                deviceConfiguration = deviceConfigurationBuilder.add();
                deviceType.save();
                deviceConfiguration.activate();

                protocolDialect1ConfigurationProperties = deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
            }
        });
    }

    public static void registerDeviceProtocol() {
        deviceProtocolPluggableClass = registerDeviceProtocol(TestProtocol.class);
    }

    private static <T extends DeviceProtocol> DeviceProtocolPluggableClass registerDeviceProtocol(Class<T> deviceProtocolClass) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newDeviceProtocolPluggableClass(deviceProtocolClass.getSimpleName(), deviceProtocolClass.getName());
        deviceProtocolPluggableClass.save();
        return deviceProtocolPluggableClass;
    }

    @AfterClass
    public static void deleteDeviceProtocol() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                deviceProtocolPluggableClass.delete();
            }
        });
    }

    @Before
    public void refreshConfigurationProperties() {
        deviceConfiguration = inMemoryPersistence.getDeviceConfigurationService()
                                    .findDeviceConfiguration(deviceConfiguration.getId())
                                    .orElseThrow(() -> new RuntimeException("Failure to reload device configuration before running next test"));
        protocolDialect1ConfigurationProperties = this.getProtocolDialectConfigurationPropertiesFromConfiguration(deviceConfiguration, DIALECT_1_NAME);
    }

    private ProtocolDialectConfigurationProperties getProtocolDialectConfigurationPropertiesFromConfiguration(DeviceConfiguration deviceConfiguration, String dialectName) {
        for (ProtocolDialectConfigurationProperties configurationProperties : deviceConfiguration.getProtocolDialectConfigurationPropertiesList()) {
            if (configurationProperties.getDeviceProtocolDialectName().equals(dialectName)) {
                return configurationProperties;
            }
        }
        return null;
    }

    @Test
    @Transactional
    public void createWithoutViolationsTest() throws BusinessException, SQLException {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createWithoutViolationsTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);

        // Business method
        device.save();

        // Asserts
        Optional<ProtocolDialectProperties> dialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME);
        assertThat(dialectProperties.isPresent()).isTrue();
        assertThat(dialectProperties.get().getDevice()).isNotNull();
        assertThat(dialectProperties.get().getDevice().getId()).isEqualTo(device.getId());
        assertThat(dialectProperties.get().getPluggableClass().getId()).isEqualTo(deviceProtocolPluggableClass.getId());
        assertThat(dialectProperties.get().getProtocolDialectConfigurationProperties().getId()).isEqualTo(protocolDialect1ConfigurationProperties.getId());
        assertThat(dialectProperties.get().getTypedProperties().size()).isEqualTo(1);
        assertThat(dialectProperties.get().getTypedProperties().getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void createAndReloadWithoutViolationsTest() throws BusinessException, SQLException {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createAndReloadWithoutViolationsTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.save();
        Device deviceReloaded = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();

        // Business method
        Optional<ProtocolDialectProperties> dialectProperties = deviceReloaded.getProtocolDialectProperties(DIALECT_1_NAME);

        // Asserts
        assertThat(dialectProperties.isPresent()).isTrue();
        assertThat(dialectProperties.get().getDevice()).isNotNull();
        assertThat(dialectProperties.get().getDevice()).isNotNull();
        assertThat(dialectProperties.get().getDevice().getId()).isEqualTo(device.getId());
        assertThat(dialectProperties.get().getPluggableClass().getId()).isEqualTo(deviceProtocolPluggableClass.getId());
        assertThat(dialectProperties.get().getProtocolDialectConfigurationProperties().getId()).isEqualTo(protocolDialect1ConfigurationProperties.getId());
        assertThat(dialectProperties.get().getTypedProperties().size()).isEqualTo(1);
        assertThat(dialectProperties.get().getTypedProperties().getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void createWithoutViolationsWithVeryLongAttributeTest() throws BusinessException, SQLException {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createWithoutViolationsTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.setProtocolDialectProperty(DIALECT_1_NAME, OPTIONAL_PROPERTY_WITH_LONG_NAME_D1, OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE);

        // Business method
        device.save();

        // Asserts
        Optional<ProtocolDialectProperties> dialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME);
        assertThat(dialectProperties.isPresent()).isTrue();
        assertThat(dialectProperties.get().getDevice()).isNotNull();
        assertThat(dialectProperties.get().getDevice().getId()).isEqualTo(device.getId());
        assertThat(dialectProperties.get().getPluggableClass().getId()).isEqualTo(deviceProtocolPluggableClass.getId());
        assertThat(dialectProperties.get().getProtocolDialectConfigurationProperties().getId()).isEqualTo(protocolDialect1ConfigurationProperties.getId());
        TypedProperties typedProperties = dialectProperties.get().getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE);
    }

    @Test
    @Transactional
    public void createAndReloadWithoutViolationsWithVeryLongAttributeTest() throws BusinessException, SQLException {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createAndReloadWithoutViolationsTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.setProtocolDialectProperty(DIALECT_1_NAME, OPTIONAL_PROPERTY_WITH_LONG_NAME_D1, OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE);
        device.save();
        Device deviceReloaded = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();

        // Business method
        Optional<ProtocolDialectProperties> dialectProperties = deviceReloaded.getProtocolDialectProperties(DIALECT_1_NAME);

        // Asserts
        assertThat(dialectProperties.isPresent()).isTrue();
        assertThat(dialectProperties.get().getDevice()).isNotNull();
        assertThat(dialectProperties.get().getDevice()).isNotNull();
        assertThat(dialectProperties.get().getDevice().getId()).isEqualTo(device.getId());
        assertThat(dialectProperties.get().getPluggableClass().getId()).isEqualTo(deviceProtocolPluggableClass.getId());
        assertThat(dialectProperties.get().getProtocolDialectConfigurationProperties().getId()).isEqualTo(protocolDialect1ConfigurationProperties.getId());
        TypedProperties typedProperties = dialectProperties.get().getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE);
    }

    @Test
    @Transactional
    public void createWithoutViolationsWhenRequiredPropertySpecifiedByInheritedPropertyTest() throws BusinessException, SQLException {
        // Add all required properties to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createWithoutViolationsWhenRequiredPropertySpecifiedByInheritedPropertyTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, OPTIONAL_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_VALUE);

        // Business method
        device.save();

        // Asserts
        ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME).get();
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();
        List<DeviceProtocolDialectProperty> properties = protocolDialectProperties.getProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.localSize()).isEqualTo(1);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getInheritedProperties().getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        this.assertPropertyNames(properties, REQUIRED_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_NAME_D1);
    }

    @Test
    @Transactional
    public void findSingleDialectsForDeviceTest() throws BusinessException, SQLException {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "findSingleDialectsForDeviceTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.save();

        // Business method
        List<ProtocolDialectProperties> dialectPropertiesList = device.getProtocolDialectPropertiesList();

        // Asserts
        assertThat(dialectPropertiesList).isNotNull();
        assertThat(dialectPropertiesList).hasSize(1);
        Set<String> dialectNames = new HashSet<>();
        for (ProtocolDialectProperties protocolDialectProperties : dialectPropertiesList) {
            dialectNames.add(protocolDialectProperties.getDeviceProtocolDialectName());
        }
        assertThat(dialectNames).containsOnly(DIALECT_1_NAME);
    }

    @Test
    @Transactional
    public void findAllDialectsForDeviceTest() throws BusinessException, SQLException {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "findAllDialectsForDeviceTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.setProtocolDialectProperty(DIALECT_2_NAME, REQUIRED_PROPERTY_NAME_D2, REQUIRED_PROPERTY_VALUE);
        device.save();

        // Business method
        List<ProtocolDialectProperties> dialectPropertiesList = device.getProtocolDialectPropertiesList();

        // Asserts
        assertThat(dialectPropertiesList).isNotNull();
        assertThat(dialectPropertiesList).hasSize(2);
        Set<String> dialectNames = new HashSet<>();
        for (ProtocolDialectProperties protocolDialectProperties : dialectPropertiesList) {
            dialectNames.add(protocolDialectProperties.getDeviceProtocolDialectName());
        }
        assertThat(dialectNames).containsOnly(DIALECT_1_NAME, DIALECT_2_NAME);
    }

    @Test
    @Transactional
    public void findNoDialectsForDevice() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "findNoDialectsForDevice", MRID);
        device.save();

        // Business method
        List<ProtocolDialectProperties> dialectPropertiesList = device.getProtocolDialectPropertiesList();

        // Asserts
        assertThat(dialectPropertiesList).isEmpty();
    }

    @Test
    @Transactional
    public void findDialect() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "findDialect", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.save();

        // Business method
        Optional<ProtocolDialectProperties> dialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME);

        // Asserts
        assertThat(dialectProperties.isPresent()).isTrue();
        assertThat(dialectProperties.get().getDeviceProtocolDialectName()).isEqualTo(DIALECT_1_NAME);
    }

    @Test
    @Transactional
    public void findDialectThatDoesNotExist() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "findDialectThatDoesNotExist", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.save();

        // Business method
        Optional<ProtocolDialectProperties> dialectProperties = device.getProtocolDialectProperties(DIALECT_2_NAME);

        // Asserts
        assertThat(dialectProperties.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void getPropertiesIncludingInheritedOnesTest() throws BusinessException, SQLException {
        // Add one optional property to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(OPTIONAL_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();

        // Business method
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "getPropertiesIncludingInheritedOnesTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.save();
        ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME).get();
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();
        List<DeviceProtocolDialectProperty> properties = protocolDialectProperties.getProperties();

        // Asserts
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getInheritedProperties().getProperty(OPTIONAL_PROPERTY_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_VALUE);

        assertThat(properties.size()).isEqualTo(2);
        assertPropertyNames(properties, REQUIRED_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_NAME_D1);
    }

    @Test
    @Transactional
    public void testInheritedPropertiesAreOverriddenByLocalProperties() throws BusinessException, SQLException {
        // Add properties to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.setProperty(OPTIONAL_PROPERTY_NAME_D1, INHERITED_OPTIONAL_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "testInheritedPropertiesAreOverriddenByLocalProperties", MRID);
        // Override the optional property
        device.setProtocolDialectProperty(DIALECT_1_NAME, OPTIONAL_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_VALUE);

        // Business method
        device.save();
        ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME).get();

        // Asserts
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getInheritedValue(OPTIONAL_PROPERTY_NAME_D1)).isEqualTo(INHERITED_OPTIONAL_PROPERTY_VALUE);

        List<DeviceProtocolDialectProperty> properties = protocolDialectProperties.getProperties();
        assertThat(properties.size()).isEqualTo(2);
        assertPropertyNames(properties, REQUIRED_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_NAME_D1);
    }

    @Test(expected = ProtocolDialectConfigurationPropertiesIsRequiredException.class)
    @Transactional
    public void createWithNonExistingConfigurationPropertiesTest() throws SQLException, BusinessException {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createWithNonExistingConfigurationPropertiesTest", MRID);

        // Business method
        device.setProtocolDialectProperty("DoesNotExist", REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);

        // Asserts: see expected exception rule
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void createWithVeryLargeProperty() throws SQLException, BusinessException {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createWithNonExistingConfigurationPropertiesTest", MRID);

        // Business method
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, Strings.repeat("A", StringFactory.MAX_SIZE + 1));
        device.save();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void getTypedPropertiesWithoutAnyRequiredOrOptionalPropertySpecs() {
        ProtocolDialectProperties protocolDialectProperties = this.newTestableProtocolDialectProperties(protocolDialect1ConfigurationProperties);

        // Business method
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();

        // Asserts
        assertThat(typedProperties.propertyNames()).isEmpty();
    }

    @Test
    @Transactional
    public void getTypedPropertiesWithoutInheritedPropertiesNamingTest() {
        TestableProtocolDialectProperties protocolDialectProperties = this.newTestableProtocolDialectProperties(protocolDialect1ConfigurationProperties);
        this.mockRequiredAndOptionalProperties(protocolDialectProperties);
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();

        // Asserts
        assertThat(typedProperties.propertyNames()).isNotEmpty();
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE);
    }

    @Test
    @Transactional
    public void getTypedPropertiesFromInheritedPropertiesNamingTest() {
        // Add properties to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1, INHERITED_OPTIONAL_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();
        TestableProtocolDialectProperties protocolDialectProperties = this.newTestableProtocolDialectProperties(protocolDialect1ConfigurationProperties);
        this.mockRequiredProperties(protocolDialectProperties);

        // Business method
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();

        // Asserts
        assertThat(typedProperties.propertyNames()).isNotEmpty();
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1)).isEqualTo(INHERITED_OPTIONAL_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void getTypedPropertiesWithOverriddenInheritedPropertyNamingTest() {
        // Add properties to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1, INHERITED_OPTIONAL_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();
        TestableProtocolDialectProperties protocolDialectProperties = this.newTestableProtocolDialectProperties(protocolDialect1ConfigurationProperties);
        this.mockRequiredAndOptionalProperties(protocolDialectProperties);

        // Business methods
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();

        // Asserts
        assertThat(typedProperties.propertyNames()).isNotEmpty();
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1)).isTrue();
        assertThat(typedProperties.getInheritedProperties().getProperty(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1)).isEqualTo(INHERITED_OPTIONAL_PROPERTY_VALUE);
    }

    private TestableProtocolDialectProperties newTestableProtocolDialectProperties(ProtocolDialectConfigurationProperties configurationProperties) {
        TestableProtocolDialectProperties properties = new TestableProtocolDialectProperties(
                inMemoryPersistence.getDataModel(),
                inMemoryPersistence.getEventService(),
                inMemoryPersistence.getThesaurus(),
                inMemoryPersistence.getClock(),
                inMemoryPersistence.getProtocolPluggableService());
        properties.initialize(mock(Device.class), configurationProperties);
        return properties;
    }

    private void mockRequiredAndOptionalProperties(TestableProtocolDialectProperties protocolDialectProperties) {
        Relation mockedRelation = this.newMockedRelation();
        this.addRequiredAndOptionalProperties(mockedRelation);
        protocolDialectProperties.addPropertyRelation(mockedRelation);
    }

    private void mockRequiredProperties(TestableProtocolDialectProperties protocolDialectProperties) {
        Relation mockedRelation = this.newMockedRelation();
        this.addRequiredProperties(mockedRelation);
        protocolDialectProperties.addPropertyRelation(mockedRelation);
    }

    private Relation newMockedRelation() {
        RelationType relationType = this.mockRelationType();
        Relation mockedRelation = mock(Relation.class);
        when(mockedRelation.getRelationType()).thenReturn(relationType);
        Instant from = Instant.now();
        when(mockedRelation.getPeriod()).thenReturn(Range.atLeast(from));
        when(mockedRelation.getFrom()).thenReturn(from);
        when(mockedRelation.getTo()).thenReturn(null);
        when(mockedRelation.includes(any(Instant.class))).thenReturn(true);
        when(mockedRelation.isObsolete()).thenReturn(false);
        return mockedRelation;
    }

    private RelationType mockRelationType() {
        RelationType relationType = mock(RelationType.class);
        RelationAttributeType requiredAttribute = mock(RelationAttributeType.class);
        when(requiredAttribute.getRelationType()).thenReturn(relationType);
        when(requiredAttribute.getRequired()).thenReturn(true);
        when(requiredAttribute.getName()).thenReturn(REQUIRED_PROPERTY_NAME_D1);
        RelationAttributeType optionalAttribute = mock(RelationAttributeType.class);
        when(optionalAttribute.getRelationType()).thenReturn(relationType);
        when(optionalAttribute.getRequired()).thenReturn(false);
        when(optionalAttribute.getName()).thenReturn(OPTIONAL_PROPERTY_NAME_D1);
        RelationAttributeType optionalAttributeWithLongName = mock(RelationAttributeType.class);
        when(optionalAttributeWithLongName.getRelationType()).thenReturn(relationType);
        when(optionalAttributeWithLongName.getRequired()).thenReturn(false);
        when(optionalAttributeWithLongName.getName()).thenReturn(OPTIONAL_PROPERTY_WITH_CONVERTED_NAME);
        when(relationType.getAttributeTypes()).thenReturn(Arrays.asList(requiredAttribute, optionalAttribute, optionalAttributeWithLongName));
        when(relationType.getAttributeType(REQUIRED_PROPERTY_NAME_D1)).thenReturn(requiredAttribute);
        when(relationType.getAttributeType(OPTIONAL_PROPERTY_NAME_D1)).thenReturn(optionalAttribute);
        when(relationType.getAttributeType(OPTIONAL_PROPERTY_WITH_CONVERTED_NAME)).thenReturn(optionalAttributeWithLongName);
        return relationType;
    }

    private void addRequiredAndOptionalProperties(Relation mockedRelation) {
        this.addRequiredProperties(mockedRelation);
        this.addProperty(mockedRelation, OPTIONAL_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_VALUE);
        this.addProperty(mockedRelation, OPTIONAL_PROPERTY_WITH_CONVERTED_NAME, OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE);
    }

    private void addRequiredProperties(Relation mockedRelation) {
        this.addProperty(mockedRelation, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
    }

    private void addProperty(Relation mockedRelation, String propertyName, String propertyValue) {
        when(mockedRelation.get(propertyName)).thenReturn(propertyValue);
        RelationAttributeType attributeType = mockedRelation.getRelationType().getAttributeType(propertyName);
        when(mockedRelation.get(attributeType)).thenReturn(propertyValue);
    }

    private void assertPropertyNames(List<DeviceProtocolDialectProperty> properties, String... propertyNames) {
        assertThat(properties.size()).isEqualTo(propertyNames.length);
        Set<String> actualPropertyNames = new HashSet<>();
        for (DeviceProtocolDialectProperty property : properties) {
            actualPropertyNames.add(property.getName());
        }
        assertThat(actualPropertyNames).containsOnly(propertyNames);
    }

    public static class TestProtocolDialect1 implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return DIALECT_1_NAME;
        }

        @Override
        public String getDisplayName() {
            return this.getDeviceProtocolDialectName();
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            List<PropertySpec> propertySpecs = new ArrayList<>(3);
            propertySpecs.add(this.requiredPropertySpec());
            propertySpecs.add(this.optionalPropertySpec());
            propertySpecs.add(this.optionalWithLongNamePropertySpec());
            return propertySpecs;
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            switch (name) {
                case REQUIRED_PROPERTY_NAME_D1: {
                    return this.requiredPropertySpec();
                }
                case OPTIONAL_PROPERTY_NAME_D1: {
                    return this.optionalPropertySpec();
                }
                case OPTIONAL_PROPERTY_WITH_LONG_NAME_D1: {
                    return this.optionalWithLongNamePropertySpec();
                }
                default: {
                    return null;
                }
            }
        }

        private PropertySpec requiredPropertySpec() {
            return new PropertySpecServiceImpl().basicPropertySpec(REQUIRED_PROPERTY_NAME_D1, true, new StringFactory());
        }

        private PropertySpec optionalPropertySpec() {
            return new PropertySpecServiceImpl().stringPropertySpec(OPTIONAL_PROPERTY_NAME_D1, false, OPTIONAL_PROPERTY_VALUE);
        }

        private PropertySpec optionalWithLongNamePropertySpec() {
            return new PropertySpecServiceImpl().basicPropertySpec(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1, false, new StringFactory());
        }

    }

    public static class TestProtocolDialect2 implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return DIALECT_2_NAME;
        }

        @Override
        public String getDisplayName() {
            return this.getDeviceProtocolDialectName();
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            List<PropertySpec> propertySpecs = new ArrayList<>(2);
            propertySpecs.add(this.requiredPropertySpec());
            propertySpecs.add(this.optionalPropertySpec());
            return propertySpecs;
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            switch (name) {
                case REQUIRED_PROPERTY_NAME_D2: {
                    return this.requiredPropertySpec();
                }
                case OPTIONAL_PROPERTY_NAME_D2: {
                    return this.optionalPropertySpec();
                }
                default: {
                    return null;
                }
            }
        }

        private PropertySpec requiredPropertySpec() {
            return RequiredPropertySpecFactory.newInstance().stringPropertySpec(REQUIRED_PROPERTY_NAME_D2);
        }

        private PropertySpec optionalPropertySpec() {
            return new PropertySpecServiceImpl().basicPropertySpec(OPTIONAL_PROPERTY_NAME_D2, false, new StringFactory());
        }

    }

    /**
     * For the purpose of testing getTypedProperties and working with mocked values.
     */
    private final class TestableProtocolDialectProperties extends ProtocolDialectPropertiesImpl {

        private List<Relation> propertyRelations = new ArrayList<>();
        private final String dialectName;

        private TestableProtocolDialectProperties(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ProtocolPluggableService protocolPluggableService) {
            this(DIALECT_1_NAME, dataModel, eventService, thesaurus, clock, protocolPluggableService);
        }

        private TestableProtocolDialectProperties(String dialectName, DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ProtocolPluggableService protocolPluggableService) {
            super(dataModel, eventService, thesaurus, clock, protocolPluggableService);
            this.dialectName = dialectName;
        }

        @Override
        public String getDeviceProtocolDialectName() {
            return this.dialectName;
        }

        private void addPropertyRelation(Relation relation) {
            this.propertyRelations.add(relation);
        }

        @Override
        public List<Relation> getRelations(RelationAttributeType attrib, Instant date, boolean includeObsolete) {
            List<Relation> filteredByDate = this.filterByDate(this.propertyRelations, date);
            if (includeObsolete) {
                return filteredByDate;
            } else {
                return this.filterObsolete(filteredByDate);
            }
        }

        private List<Relation> filterByDate(List<Relation> relations, Instant date) {
            List<Relation> activeOnDate = new ArrayList<>(relations.size());    // Worst case: all relations are active on the specified Date
            for (Relation relation : relations) {
                if (relation.includes(date)) {
                    activeOnDate.add(relation);
                }
            }
            return activeOnDate;
        }

        private List<Relation> filterObsolete(List<Relation> relations) {
            List<Relation> onlyActive = new ArrayList<>(relations.size());  // Worst case: no obsolete relations
            for (Relation relation : relations) {
                if (!relation.isObsolete()) {
                    onlyActive.add(relation);
                }
            }
            return onlyActive;
        }

    }

}