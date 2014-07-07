package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceUsageType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.exceptions.ProtocolDialectConfigurationPropertiesIsRequiredException;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectProperty;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 26/04/13
 * Time: 16:46
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectPropertiesImplIT extends PersistenceIntegrationTest {

    private static final int PROTOCOL_DIALECT_PROPERTIES_ID = 6514;
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
    private static ProtocolDialectConfigurationProperties protocolDialect2ConfigurationProperties;

    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceProtocolDialect deviceProtocolDialect;

    private Interval relationActivePeriod = new Interval(new Date(), null);

    @BeforeClass
    public static void setupDeviceProtocolAndDeviceConfiguration () {
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    registerDeviceProtocol();
                    deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(ProtocolDialectPropertiesImplIT.class.getSimpleName(), deviceProtocolPluggableClass);
                    deviceType.setDeviceUsageType(DeviceUsageType.METER);
                    DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(ProtocolDialectPropertiesImplIT.class.getName());
                    deviceConfiguration = deviceConfigurationBuilder.add();
                    deviceType.save();
                    deviceConfiguration.activate();

                    DeviceCommunicationConfiguration deviceCommunicationConfiguration = inMemoryPersistence.getDeviceConfigurationService().newDeviceCommunicationConfiguration(deviceConfiguration);
                    deviceCommunicationConfiguration.save();
                    protocolDialect1ConfigurationProperties = deviceCommunicationConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
                    protocolDialect2ConfigurationProperties = deviceCommunicationConfiguration.getProtocolDialectConfigurationPropertiesList().get(1);
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    public static void registerDeviceProtocol () {
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
    public static void deleteDeviceProtocol () {
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    deviceProtocolPluggableClass.delete();
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    @Before
    public void refreshConfigurationProperties () {
        deviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfiguration(deviceConfiguration.getId());
        DeviceCommunicationConfiguration communicationConfiguration = deviceConfiguration.getCommunicationConfiguration();
        protocolDialect1ConfigurationProperties = this.getProtocolDialectConfigurationPropertiesFromConfiguration(communicationConfiguration, DIALECT_1_NAME);
        protocolDialect2ConfigurationProperties = getProtocolDialectConfigurationPropertiesFromConfiguration(communicationConfiguration, DIALECT_2_NAME);
    }

    private ProtocolDialectConfigurationProperties getProtocolDialectConfigurationPropertiesFromConfiguration(DeviceCommunicationConfiguration communicationConfiguration, String dialectName) {
        for (ProtocolDialectConfigurationProperties configurationProperties : communicationConfiguration.getProtocolDialectConfigurationPropertiesList()) {
            if (configurationProperties.getDeviceProtocolDialectName().equals(dialectName)) {
                return configurationProperties;
            }
        }
        return null;
    }

    @Test
    @Transactional
    public void createWithoutViolationsTest() throws BusinessException, SQLException {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "createWithoutViolationsTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);

        // Business method
        device.save();

        // Asserts
        ProtocolDialectProperties dialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME);
        assertThat(dialectProperties).isNotNull();
        assertThat(dialectProperties.getDevice()).isNotNull();
        assertThat(dialectProperties.getDevice().getId()).isEqualTo(device.getId());
        assertThat(dialectProperties.getPluggableClass().getId()).isEqualTo(deviceProtocolPluggableClass.getId());
        assertThat(dialectProperties.getProtocolDialectConfigurationProperties().getId()).isEqualTo(protocolDialect1ConfigurationProperties.getId());
        assertThat(dialectProperties.getTypedProperties().size()).isEqualTo(1);
        assertThat(dialectProperties.getTypedProperties().getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void createAndReloadWithoutViolationsTest() throws BusinessException, SQLException {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "createAndReloadWithoutViolationsTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.save();
        Device deviceReloaded = inMemoryPersistence.getDeviceDataService().findDeviceById(device.getId());

        // Business method
        ProtocolDialectProperties dialectProperties = deviceReloaded.getProtocolDialectProperties(DIALECT_1_NAME);

        // Asserts
        assertThat(dialectProperties).isNotNull();
        assertThat(dialectProperties.getDevice()).isNotNull();
        assertThat(dialectProperties.getDevice()).isNotNull();
        assertThat(dialectProperties.getDevice().getId()).isEqualTo(device.getId());
        assertThat(dialectProperties.getPluggableClass().getId()).isEqualTo(deviceProtocolPluggableClass.getId());
        assertThat(dialectProperties.getProtocolDialectConfigurationProperties().getId()).isEqualTo(protocolDialect1ConfigurationProperties.getId());
        assertThat(dialectProperties.getTypedProperties().size()).isEqualTo(1);
        assertThat(dialectProperties.getTypedProperties().getProperty(REQUIRED_PROPERTY_NAME_D1)).isEqualTo(REQUIRED_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void createWithoutViolationsWhenRequiredPropertySpecifiedByInheritedPropertyTest() throws BusinessException, SQLException {
        // Add all required properties to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "createWithoutViolationsWhenRequiredPropertySpecifiedByInheritedPropertyTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, OPTIONAL_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_VALUE);

        // Business method
        device.save();

        // Asserts
        ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "findSingleDialectsForDeviceTest", MRID);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "findAllDialectsForDeviceTest", MRID);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "findNoDialectsForDevice", MRID);
        device.save();

        // Business method
        List<ProtocolDialectProperties> dialectPropertiesList = device.getProtocolDialectPropertiesList();

        // Asserts
        assertThat(dialectPropertiesList).isEmpty();
    }

    @Test
    @Transactional
    public void findDialect() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "findDialect", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.save();

        // Business method
        ProtocolDialectProperties dialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME);

        // Asserts
        assertThat(dialectProperties).isNotNull();
        assertThat(dialectProperties.getDeviceProtocolDialectName()).isEqualTo(DIALECT_1_NAME);
    }

    @Test
    @Transactional
    public void findDialectThatDoesNotExist() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "findDialectThatDoesNotExist", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.save();

        // Business method
        ProtocolDialectProperties dialectProperties = device.getProtocolDialectProperties(DIALECT_2_NAME);

        // Asserts
        assertThat(dialectProperties).isNull();
    }

    @Test
    @Transactional
    public void getPropertiesIncludingInheritedOnesTest() throws BusinessException, SQLException {
        // Add one optional property to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(OPTIONAL_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();

        // Business method
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "getPropertiesIncludingInheritedOnesTest", MRID);
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);
        device.save();
        ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME);
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

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "testInheritedPropertiesAreOverriddenByLocalProperties", MRID);
        // Override the optional property
        device.setProtocolDialectProperty(DIALECT_1_NAME, OPTIONAL_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_VALUE);

        // Business method
        device.save();
        ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME);

        // Asserts
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME_D1)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getInheritedProperties().getProperty(OPTIONAL_PROPERTY_NAME_D1)).isEqualTo(INHERITED_OPTIONAL_PROPERTY_VALUE);

        List<DeviceProtocolDialectProperty> properties = protocolDialectProperties.getProperties();
        assertThat(properties.size()).isEqualTo(2);
        assertPropertyNames(properties, REQUIRED_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_NAME_D1);
    }

    @Test(expected = ProtocolDialectConfigurationPropertiesIsRequiredException.class)
    @Transactional
    public void createWithNonExistingConfigurationPropertiesTest() throws SQLException, BusinessException {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "createWithNonExistingConfigurationPropertiesTest", MRID);

        // Business method
        device.setProtocolDialectProperty("DoesNotExist", REQUIRED_PROPERTY_NAME_D1, REQUIRED_PROPERTY_VALUE);

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
                inMemoryPersistence.getDeviceDataService().getDataModel(),
                inMemoryPersistence.getEventService(),
                inMemoryPersistence.getDeviceDataService().getThesaurus(),
                inMemoryPersistence.getRelationService(),
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
        Date from = new Date();
        when(mockedRelation.getPeriod()).thenReturn(Interval.startAt(from));
        when(mockedRelation.getFrom()).thenReturn(from);
        when(mockedRelation.getTo()).thenReturn(null);
        when(mockedRelation.includes(any(Date.class))).thenReturn(true);
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

    public static class TestProtocol implements DeviceProtocol {
        @Override
        public void setPropertySpecService(PropertySpecService propertySpecService) {

        }

        @Override
        public void init(OfflineDevice offlineDevice, ComChannel comChannel) {

        }

        @Override
        public void terminate() {

        }

        @Override
        public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
            return null;
        }

        @Override
        public String getProtocolDescription() {
            return null;
        }

        @Override
        public DeviceFunction getDeviceFunction() {
            return null;
        }

        @Override
        public ManufacturerInformation getManufacturerInformation() {
            return null;
        }

        @Override
        public List<ConnectionType> getSupportedConnectionTypes() {
            return null;
        }

        @Override
        public void logOn() {

        }

        @Override
        public void daisyChainedLogOn() {

        }

        @Override
        public void logOff() {

        }

        @Override
        public void daisyChainedLogOff() {

        }

        @Override
        public String getSerialNumber() {
            return null;
        }

        @Override
        public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {

        }

        @Override
        public DeviceProtocolCache getDeviceCache() {
            return null;
        }

        @Override
        public void setTime(Date timeToSet) {

        }

        @Override
        public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
            return null;
        }

        @Override
        public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
            return null;
        }

        @Override
        public Date getTime() {
            return null;
        }

        @Override
        public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
            return null;
        }

        @Override
        public List<DeviceMessageSpec> getSupportedMessages() {
            return null;
        }

        @Override
        public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
            return null;
        }

        @Override
        public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
            return null;
        }

        @Override
        public String format(PropertySpec propertySpec, Object messageAttribute) {
            return null;
        }

        @Override
        public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
            DeviceProtocolDialect protocolDialect1 = new TestProtocolDialect1();
            DeviceProtocolDialect protocolDialect2 = new TestProtocolDialect2();
            return Arrays.asList(protocolDialect1, protocolDialect2);
        }

        @Override
        public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {

        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public String getSecurityRelationTypeName() {
            return null;
        }

        @Override
        public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public PropertySpec getSecurityPropertySpec(String name) {
            return null;
        }

        @Override
        public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
            return null;
        }

        @Override
        public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {

        }

        @Override
        public CollectedTopology getDeviceTopology() {
            return null;
        }

        @Override
        public String getVersion() {
            return "For Testing Purposes only";
        }

        @Override
        public void copyProperties(TypedProperties properties) {

        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            return null;
        }
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

        private PropertySpec<String> requiredPropertySpec () {
            return RequiredPropertySpecFactory.newInstance().stringPropertySpec(REQUIRED_PROPERTY_NAME_D1);
        }

        private PropertySpec<String> optionalPropertySpec () {
            return OptionalPropertySpecFactory.newInstance().stringPropertySpec(OPTIONAL_PROPERTY_NAME_D1, OPTIONAL_PROPERTY_VALUE);
        }

        private PropertySpec<String> optionalWithLongNamePropertySpec () {
            return OptionalPropertySpecFactory.newInstance().stringPropertySpec(OPTIONAL_PROPERTY_WITH_LONG_NAME_D1);
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

        private PropertySpec<String> requiredPropertySpec () {
            return RequiredPropertySpecFactory.newInstance().stringPropertySpec(REQUIRED_PROPERTY_NAME_D2);
        }

        private PropertySpec<String> optionalPropertySpec () {
            return OptionalPropertySpecFactory.newInstance().stringPropertySpec(OPTIONAL_PROPERTY_NAME_D2);
        }

    }

    /**
     * For the purpose of testing getTypedProperties and working with mocked values.
     */
    private final class TestableProtocolDialectProperties extends ProtocolDialectPropertiesImpl {

        private List<Relation> propertyRelations = new ArrayList<>();
        private final String dialectName;

        private TestableProtocolDialectProperties(DataModel dataModel, EventService eventService, Thesaurus thesaurus, RelationService relationService, Clock clock, ProtocolPluggableService protocolPluggableService) {
            this(DIALECT_1_NAME, dataModel, eventService, thesaurus, relationService, clock, protocolPluggableService);
        }

        private TestableProtocolDialectProperties(String dialectName, DataModel dataModel, EventService eventService, Thesaurus thesaurus, RelationService relationService, Clock clock, ProtocolPluggableService protocolPluggableService) {
            super(dataModel, eventService, thesaurus, relationService, clock, protocolPluggableService);
            this.dialectName = dialectName;
        }

        @Override
        public String getDeviceProtocolDialectName() {
            return this.dialectName;
        }

        private void addPropertyRelation (Relation relation) {
            this.propertyRelations.add(relation);
        }

        @Override
        public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete) {
            List<Relation> filteredByDate = this.filterByDate(this.propertyRelations, date);
            if (includeObsolete) {
                return filteredByDate;
            }
            else {
                return this.filterObsolete(filteredByDate);
            }
        }

        private List<Relation> filterByDate(List<Relation> relations, Date date) {
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