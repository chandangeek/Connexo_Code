/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceUsageType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.exceptions.ProtocolDialectConfigurationPropertiesIsRequiredException;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectProperty;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.base.Strings;
import com.google.inject.Module;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Size;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectPropertiesImplIT extends PersistenceIntegrationTest {

    public static final String REQUIRED_PROPERTY_NAME = "ThisIsTheRequiredPropertyName";
    private static final String REQUIRED_PROPERTY_VALUE = "lmskdjfsmldkfjsqlmdkfj";
    private static final String OPTIONAL_PROPERTY_NAME = "ThisIsTheOptionalPropertyName";
    private static final String OPTIONAL_PROPERTY_VALUE = "sdlfkjnsqdlmfjsqdfsqdfsqdf";
    private static final String OPTIONAL_PROPERTY_WITH_CONVERTED_NAME = "OptionalPropertyWith1227106396";
    private static final String OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE = "jklmdsqfjkldsqlozidkcxjnnclsqkdkjoijfze65465zef65e6f51ze6f51zefze";
    private static final String INHERITED_OPTIONAL_PROPERTY_VALUE = "inheritedmqjdsflmdsqkjflmsqdjkfmsqldkfjlmdsqjkf";

    public static final String DIALECT_1_NAME = TestProtocolDialect1.class.getSimpleName();
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
    public void createWithoutViolationsTest() throws SQLException {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createWithoutViolationsTest", MRID, Instant.now());
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);

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
        assertThat(dialectProperties.get().getTypedProperties().getProperty(REQUIRED_PROPERTY_NAME)).isEqualTo(REQUIRED_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void createAndReloadWithoutViolationsTest() throws SQLException {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createAndReloadWithoutViolationsTest", MRID, Instant.now());
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
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
        assertThat(dialectProperties.get().getTypedProperties().getProperty(REQUIRED_PROPERTY_NAME)).isEqualTo(REQUIRED_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void createWithoutViolationsWhenRequiredPropertySpecifiedByInheritedPropertyTest() throws SQLException {
        // Add all required properties to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createWithoutViolationsWhenRequiredPropertySpecifiedByInheritedPropertyTest", MRID, Instant
                        .now());
        device.setProtocolDialectProperty(DIALECT_1_NAME, OPTIONAL_PROPERTY_NAME, OPTIONAL_PROPERTY_VALUE);

        // Business method
        device.save();

        // Asserts
        ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME).get();
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();
        List<DeviceProtocolDialectProperty> properties = protocolDialectProperties.getProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.localSize()).isEqualTo(1);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getInheritedProperties().getProperty(REQUIRED_PROPERTY_NAME)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        this.assertPropertyNames(properties, REQUIRED_PROPERTY_NAME, OPTIONAL_PROPERTY_NAME);
    }

    @Test
    @Transactional
    public void findSingleDialectsForDeviceTest() throws SQLException {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "findSingleDialectsForDeviceTest", MRID, Instant.now());
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
        device.save();

        // Business method
        List<ProtocolDialectProperties> dialectPropertiesList = device.getProtocolDialectPropertiesList();

        // Asserts
        assertThat(dialectPropertiesList).isNotNull();
        assertThat(dialectPropertiesList).hasSize(1);
        Set<String> dialectNames =
                dialectPropertiesList
                        .stream()
                        .map(ProtocolDialectProperties::getDeviceProtocolDialectName)
                        .collect(Collectors.toSet());
        assertThat(dialectNames).containsOnly(DIALECT_1_NAME);
    }

    @Test
    @Transactional
    public void findAllDialectsForDeviceTest() throws SQLException {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "findAllDialectsForDeviceTest", MRID, Instant.now());
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
        device.setProtocolDialectProperty(DIALECT_2_NAME, REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
        device.save();

        // Business method
        List<ProtocolDialectProperties> dialectPropertiesList = device.getProtocolDialectPropertiesList();

        // Asserts
        assertThat(dialectPropertiesList).isNotNull();
        assertThat(dialectPropertiesList).hasSize(2);
        Set<String> dialectNames =
                dialectPropertiesList
                        .stream()
                        .map(ProtocolDialectProperties::getDeviceProtocolDialectName)
                        .collect(Collectors.toSet());
        assertThat(dialectNames).containsOnly(DIALECT_1_NAME, DIALECT_2_NAME);
    }

    @Test
    @Transactional
    public void findNoDialectsForDevice() {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "findNoDialectsForDevice", MRID, Instant.now());
        device.save();

        // Business method
        List<ProtocolDialectProperties> dialectPropertiesList = device.getProtocolDialectPropertiesList();

        // Asserts
        assertThat(dialectPropertiesList).isEmpty();
    }

    @Test
    @Transactional
    public void findDialect() {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "findDialect", MRID, Instant.now());
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
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
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "findDialectThatDoesNotExist", MRID, Instant.now());
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
        device.save();

        // Business method
        Optional<ProtocolDialectProperties> dialectProperties = device.getProtocolDialectProperties(DIALECT_2_NAME);

        // Asserts
        assertThat(dialectProperties.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void getPropertiesIncludingInheritedOnesTest() throws SQLException {
        // Add one optional property to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(OPTIONAL_PROPERTY_NAME, OPTIONAL_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();

        // Business method
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "getPropertiesIncludingInheritedOnesTest", MRID, Instant.now());
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
        device.save();
        ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME).get();
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();
        List<DeviceProtocolDialectProperty> properties = protocolDialectProperties.getProperties();

        // Asserts
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getInheritedProperties().getProperty(OPTIONAL_PROPERTY_NAME)).isEqualTo(OPTIONAL_PROPERTY_VALUE);

        assertThat(properties.size()).isEqualTo(2);
        assertPropertyNames(properties, REQUIRED_PROPERTY_NAME, OPTIONAL_PROPERTY_NAME);
    }

    @Test
    @Transactional
    public void testInheritedPropertiesAreOverriddenByLocalProperties() throws SQLException {
        // Add properties to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.setProperty(OPTIONAL_PROPERTY_NAME, INHERITED_OPTIONAL_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "testInheritedPropertiesAreOverriddenByLocalProperties", MRID, Instant.now());
        // Override the optional property
        device.setProtocolDialectProperty(DIALECT_1_NAME, OPTIONAL_PROPERTY_NAME, OPTIONAL_PROPERTY_VALUE);

        // Business method
        device.save();
        ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(DIALECT_1_NAME).get();

        // Asserts
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getInheritedValue(OPTIONAL_PROPERTY_NAME)).isEqualTo(INHERITED_OPTIONAL_PROPERTY_VALUE);

        List<DeviceProtocolDialectProperty> properties = protocolDialectProperties.getProperties();
        assertThat(properties.size()).isEqualTo(2);
        assertPropertyNames(properties, REQUIRED_PROPERTY_NAME, OPTIONAL_PROPERTY_NAME);
    }

    @Test(expected = ProtocolDialectConfigurationPropertiesIsRequiredException.class)
    @Transactional
    public void createWithNonExistingConfigurationPropertiesTest() throws SQLException {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createWithNonExistingConfigurationPropertiesTest", MRID, Instant.now());

        // Business method
        device.setProtocolDialectProperty("DoesNotExist", REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);

        // Asserts: see expected exception rule
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void createWithVeryLargeProperty() throws SQLException {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createWithNonExistingConfigurationPropertiesTest", MRID, Instant.now());

        // Business method
        device.setProtocolDialectProperty(DIALECT_1_NAME, REQUIRED_PROPERTY_NAME, Strings.repeat("A", StringFactory.MAX_SIZE + 1));
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
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME)).isEqualTo(REQUIRED_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void getTypedPropertiesFromInheritedPropertiesNamingTest() {
        // Add properties to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(OPTIONAL_PROPERTY_NAME, INHERITED_OPTIONAL_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();
        TestableProtocolDialectProperties protocolDialectProperties = this.newTestableProtocolDialectProperties(protocolDialect1ConfigurationProperties);
        this.mockRequiredProperties(protocolDialectProperties);

        // Business method
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();

        // Asserts
        assertThat(typedProperties.propertyNames()).isNotEmpty();
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME)).isEqualTo(INHERITED_OPTIONAL_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void getTypedPropertiesWithOverriddenInheritedPropertyNamingTest() {
        // Add properties to the configuration level first
        protocolDialect1ConfigurationProperties.setProperty(OPTIONAL_PROPERTY_NAME, INHERITED_OPTIONAL_PROPERTY_VALUE);
        protocolDialect1ConfigurationProperties.save();
        TestableProtocolDialectProperties protocolDialectProperties = this.newTestableProtocolDialectProperties(protocolDialect1ConfigurationProperties);
        this.mockRequiredAndOptionalProperties(protocolDialectProperties);

        // Business methods
        TypedProperties typedProperties = protocolDialectProperties.getTypedProperties();

        // Asserts
        assertThat(typedProperties.propertyNames()).isNotEmpty();
        assertThat(typedProperties.getProperty(REQUIRED_PROPERTY_NAME)).isEqualTo(REQUIRED_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(OPTIONAL_PROPERTY_NAME)).isEqualTo(OPTIONAL_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(OPTIONAL_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getInheritedProperties().getProperty(OPTIONAL_PROPERTY_NAME)).isEqualTo(INHERITED_OPTIONAL_PROPERTY_VALUE);
    }

    private TestableProtocolDialectProperties newTestableProtocolDialectProperties(ProtocolDialectConfigurationProperties configurationProperties) {
        TestableProtocolDialectProperties properties = new TestableProtocolDialectProperties(
                inMemoryPersistence.getDataModel(),
                inMemoryPersistence.getEventService(),
                inMemoryPersistence.getThesaurusFromDeviceDataModel(),
                inMemoryPersistence.getClock(),
                inMemoryPersistence.getProtocolPluggableService(),
                inMemoryPersistence.getCustomPropertySetService());
        properties.initialize(mock(Device.class), configurationProperties);
        return properties;
    }

    private void mockRequiredAndOptionalProperties(TestableProtocolDialectProperties protocolDialectProperties) {
        CustomPropertySetValues values = this.newCustomPropertySetValues();
        this.addRequiredAndOptionalProperties(values);
        protocolDialectProperties.addPropertyRelation(values);
    }

    private void mockRequiredProperties(TestableProtocolDialectProperties protocolDialectProperties) {
        CustomPropertySetValues values = this.newCustomPropertySetValues();
        this.addRequiredProperties(values);
        protocolDialectProperties.addPropertyRelation(values);
    }

    private CustomPropertySetValues newCustomPropertySetValues() {
        return CustomPropertySetValues.emptyFrom(Instant.now());
    }

    private void addRequiredAndOptionalProperties(CustomPropertySetValues values) {
        this.addRequiredProperties(values);
        this.addProperty(values, OPTIONAL_PROPERTY_NAME, OPTIONAL_PROPERTY_VALUE);
        this.addProperty(values, OPTIONAL_PROPERTY_WITH_CONVERTED_NAME, OPTIONAL_PROPERTY_WITH_LONG_NAME_VALUE);
    }

    private void addRequiredProperties(CustomPropertySetValues values) {
        this.addProperty(values, REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_VALUE);
    }

    private void addProperty(CustomPropertySetValues values, String propertyName, String propertyValue) {
        values.setProperty(propertyName, propertyValue);
    }

    private void assertPropertyNames(List<DeviceProtocolDialectProperty> properties, String... propertyNames) {
        assertThat(properties.size()).isEqualTo(propertyNames.length);
        Set<String> actualPropertyNames =
                properties
                        .stream()
                        .map(DeviceProtocolDialectProperty::getName)
                        .collect(Collectors.toSet());
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
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.of(new ProtocolDialectCustomPropertySet());
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
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.of(new ProtocolDialectCustomPropertySet());
        }

    }

    public static class PersistentProtocolDialectProperties extends CommonDeviceProtocolDialectProperties {
        @Size(max=Table.MAX_STRING_LENGTH)
        private String required;
        @Size(max=Table.MAX_STRING_LENGTH)
        private String optional;

        @Override
        protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
            this.required = (String) propertyValues.getProperty(REQUIRED_PROPERTY_NAME);
            this.optional = (String) propertyValues.getProperty(OPTIONAL_PROPERTY_NAME);
        }

        @Override
        protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
            this.setPropertyIfNotNull(propertySetValues, REQUIRED_PROPERTY_NAME, this.required);
            this.setPropertyIfNotNull(propertySetValues, OPTIONAL_PROPERTY_NAME, this.optional);
        }

        @Override
        public void validateDelete() {
            // nothing to validate
        }
    }

    public static class ProtocolDialectPropertiesPersistenceSupport implements PersistenceSupport<DeviceProtocolDialectPropertyProvider, PersistentProtocolDialectProperties> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "T01";
        }

        @Override
        public String tableName() {
            return "DDC_TST_DIALECT";
        }

        @Override
        public String domainForeignKeyName() {
            return "FK_DDC_TSTDIALECT_PROPS";
        }

        @Override
        public String domainFieldName() {
            return CommonDeviceProtocolDialectProperties.Fields.DIALECT_PROPERTY_PROVIDER.javaName();
        }

        @Override
        public String domainColumnName() {
            return CommonDeviceProtocolDialectProperties.Fields.DIALECT_PROPERTY_PROVIDER.databaseName();
        }

        @Override
        public Class<PersistentProtocolDialectProperties> persistenceClass() {
            return PersistentProtocolDialectProperties.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table
                .column("REQUIRED")
                .varChar()
                .map("required")
                .add();
            table
                .column("OPTIONAL")
                .varChar()
                .map("optional")
                .add();
        }
    }

    public static class ProtocolDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, PersistentProtocolDialectProperties> {
        @Override
        public String getId() {
            return ProtocolDialectPropertiesImplIT.class.getSimpleName() + ProtocolDialectCustomPropertySet.class.getSimpleName();
        }

        @Override
        public String getName() {
            return ProtocolDialectCustomPropertySet.class.getName();
        }

        @Override
        public Class<DeviceProtocolDialectPropertyProvider> getDomainClass() {
            return DeviceProtocolDialectPropertyProvider.class;
        }

        @Override
        public String getDomainClassDisplayName() {
            return this.getDomainClass().getName();
        }

        @Override
        public PersistenceSupport<DeviceProtocolDialectPropertyProvider, PersistentProtocolDialectProperties> getPersistenceSupport() {
            return new ProtocolDialectPropertiesPersistenceSupport();
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public boolean isVersioned() {
            return true;
        }

        @Override
        public Set<ViewPrivilege> defaultViewPrivileges() {
            return EnumSet.noneOf(ViewPrivilege.class);
        }

        @Override
        public Set<EditPrivilege> defaultEditPrivileges() {
            return EnumSet.noneOf(EditPrivilege.class);
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            PropertySpecServiceImpl propertySpecService = new PropertySpecServiceImpl();
            return Arrays.asList(
                propertySpecService
                        .stringSpec()
                        .named(REQUIRED_PROPERTY_NAME, REQUIRED_PROPERTY_NAME)
                        .describedAs("Description for required property")
                        .markRequired()
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(OPTIONAL_PROPERTY_NAME, OPTIONAL_PROPERTY_NAME)
                        .describedAs("Description for optional property")
                        .finish());
        }
    }

    /**
     * For the purpose of testing getTypedProperties and working with mocked values.
     */
    private final class TestableProtocolDialectProperties extends ProtocolDialectPropertiesImpl {

        private CustomPropertySetValues values = CustomPropertySetValues.empty();
        private final String dialectName;

        private TestableProtocolDialectProperties(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ProtocolPluggableService protocolPluggableService, CustomPropertySetService customPropertySetService) {
            this(DIALECT_1_NAME, dataModel, eventService, thesaurus, clock, protocolPluggableService, customPropertySetService);
        }

        private TestableProtocolDialectProperties(String dialectName, DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ProtocolPluggableService protocolPluggableService, CustomPropertySetService customPropertySetService) {
            super(dataModel, eventService, thesaurus, clock, protocolPluggableService, customPropertySetService);
            this.dialectName = dialectName;
        }

        @Override
        public String getDeviceProtocolDialectName() {
            return this.dialectName;
        }

        @Override
        protected List<DeviceProtocolDialectProperty> toProperties(CustomPropertySetValues values) {
            return super.toProperties(this.values);
        }

        private void addPropertyRelation(CustomPropertySetValues values) {
            this.values = values;
        }

    }

}