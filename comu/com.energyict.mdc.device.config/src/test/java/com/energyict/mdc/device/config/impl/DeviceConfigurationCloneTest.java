package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import org.fest.assertions.core.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 08/07/15
 * Time: 10:29
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigurationCloneTest extends PersistenceTest {

    private final String DEVICE_TYPE_NAME = DeviceConfigurationCloneTest.class.getName() + "Type";
    private final String propertyName1 = "PropName";
    private final String propertyName2 = "OtherPropName";
    private final String protocolDialectName1 = "Dialect1";
    private final String protocolDialectName2 = "Dialect2";

    @Test
    @Transactional
    public void cloneEmptyDeviceConfigurationTest() {
        DeviceType deviceType = createSimpleDeviceType();
        DeviceConfiguration emptyDeviceConfiguration = deviceType.newConfiguration("EmptyDeviceConfiguration").add();

        DeviceConfiguration clonedEmptyDeviceConfiguration = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(emptyDeviceConfiguration, "ClonedEmptyDeviceConfiguration");

        verifyBasicStuffForClonedConfigs(emptyDeviceConfiguration, clonedEmptyDeviceConfiguration);
        assertThat(emptyDeviceConfiguration.canActAsGateway()).isFalse();
        assertThat(clonedEmptyDeviceConfiguration.canActAsGateway()).isFalse();
        assertThat(emptyDeviceConfiguration.isDirectlyAddressable()).isFalse();
        assertThat(clonedEmptyDeviceConfiguration.isDirectlyAddressable()).isFalse();
    }

    @Test
    @Transactional
    public void cloneActiveEmptyDeviceConfigurationTest() {
        DeviceType deviceType = createSimpleDeviceType();
        DeviceConfiguration activeConfig = deviceType.newConfiguration("EmptyActiveConfig").add();
        activeConfig.activate();

        DeviceConfiguration clonedActiveConfig = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(activeConfig, "ClonedEmptyDeviceConfiguration");

        assertThat(activeConfig.isActive()).isTrue();
        verifyBasicStuffForClonedConfigs(activeConfig, clonedActiveConfig);
    }

    @Test
    @Transactional
    public void cloneGateWayTypeTest() {
        DeviceType deviceType = createSimpleDeviceType();
        DeviceConfiguration activeConfig = deviceType.newConfiguration("cloneGateWayTypeTest").gatewayType(GatewayType.LOCAL_AREA_NETWORK).add();

        DeviceConfiguration clone = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(activeConfig, "ClonedGatewayType");
        assertThat(clone.getGetwayType()).isEqualTo(GatewayType.LOCAL_AREA_NETWORK);
    }

    @Test
    @Transactional
    public void cloneWithDescriptionTest() {
        DeviceType deviceType = createSimpleDeviceType();
        String description = "My very nice and detailed description";
        DeviceConfiguration activeConfig = deviceType.newConfiguration("DescriptionTest").description(description).add();

        DeviceConfiguration clone = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(activeConfig, "cloneWithDescriptionTest");
        assertThat(clone.getDescription()).isEqualTo(description);
    }

    @Test
    @Transactional
    public void canActAsGatewayTest() {
        enhanceDeviceProtocolWithCanActAsGatewayCapability();
        DeviceType deviceType = createSimpleDeviceType();
        DeviceConfiguration activeConfig = deviceType.newConfiguration("CanActAsGateway").canActAsGateway(true).gatewayType(GatewayType.LOCAL_AREA_NETWORK).add();

        DeviceConfiguration clone = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(activeConfig, "cloneWithCanActAsGateway");
        assertThat(clone.canActAsGateway()).isTrue();
    }

    private void enhanceDeviceProtocolWithCanActAsGatewayCapability() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
    }

    @Test
    @Transactional
    public void isDirectlyAddressableTest() {
        enhanceDeviceProtocolWithDirectlyAddressableCapability();
        DeviceType deviceType = createSimpleDeviceType();
        DeviceConfiguration activeConfig = deviceType.newConfiguration("isDirectlyAddressableTest").isDirectlyAddressable(true).add();

        DeviceConfiguration clone = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(activeConfig, "cloneIsDirectlyAddressableTest");
        assertThat(clone.isDirectlyAddressable()).isTrue();
    }

    private void enhanceDeviceProtocolWithDirectlyAddressableCapability() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION));
    }

    @Test
    @Transactional
    public void cloneWithGeneralAttributesTest() {
        String propertyValue1 = "PropValue";
        long propertyValue2 = 123455L;
        enhanceDeviceProtocolWithPropertySpecs(propertyName1, propertyName2);
        DeviceType deviceType = createSimpleDeviceType();
        DeviceConfiguration emptyDeviceConfiguration = deviceType.newConfiguration("ConfigWithGeneralAttributes").add();
        emptyDeviceConfiguration.getDeviceProtocolProperties().setProperty(propertyName1, propertyValue1);
        emptyDeviceConfiguration.getDeviceProtocolProperties().setProperty(propertyName2, propertyValue2);
        emptyDeviceConfiguration.save();

        DeviceConfiguration clone = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(emptyDeviceConfiguration, "ClonedEmptyDeviceConfiguration");

        assertThat(emptyDeviceConfiguration.getDeviceProtocolProperties().getProperty(propertyName1)).isEqualTo(propertyValue1);
        assertThat(emptyDeviceConfiguration.getDeviceProtocolProperties().getProperty(propertyName2)).isEqualTo(propertyValue2);
        verifyBasicStuffForClonedConfigs(emptyDeviceConfiguration, clone);
        assertThat(clone.getDeviceProtocolProperties().getProperty(propertyName1)).isEqualTo(propertyValue1);
        assertThat(clone.getDeviceProtocolProperties().getProperty(propertyName2)).isEqualTo(propertyValue2);
    }

    @Test
    @Transactional
    public void cloneWithProtocolDialectPropertiesTest() {
        String propertyValue1 = "PropValue";
        long propertyValue2 = 123455L;

        DeviceProtocolDialect dialect1 = mockDeviceProtocolDialect(propertyName1, protocolDialectName1, inMemoryPersistence.getPropertySpecService().stringPropertySpec(propertyName1, false, null));
        DeviceProtocolDialect dialect2 = mockDeviceProtocolDialect(propertyName2, protocolDialectName2, inMemoryPersistence.getPropertySpecService().longPropertySpec(propertyName2, false, null));

        when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Arrays.asList(dialect1, dialect2));

        DeviceType deviceType = createSimpleDeviceType();
        DeviceConfiguration configWithDialects = deviceType.newConfiguration("ConfigWithDialects").add();
        configWithDialects.findOrCreateProtocolDialectConfigurationProperties(dialect1).setProperty(propertyName1, propertyValue1);
        configWithDialects.findOrCreateProtocolDialectConfigurationProperties(dialect2).setProperty(propertyName2, propertyValue2);
        configWithDialects.save();

        DeviceConfiguration clone = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(configWithDialects, "ClonedEmptyDeviceConfiguration");

        assertThat(configWithDialects.getProtocolDialectConfigurationPropertiesList()).hasSize(2);
        assertThat(configWithDialects.findOrCreateProtocolDialectConfigurationProperties(dialect1).getProperty(propertyName1)).isEqualTo(propertyValue1);
        assertThat(configWithDialects.findOrCreateProtocolDialectConfigurationProperties(dialect2).getProperty(propertyName2)).isEqualTo(propertyValue2);
        verifyBasicStuffForClonedConfigs(configWithDialects, clone);
        assertThat(clone.getProtocolDialectConfigurationPropertiesList()).hasSize(2);
        assertThat(clone.findOrCreateProtocolDialectConfigurationProperties(dialect1).getProperty(propertyName1)).isEqualTo(propertyValue1);
        assertThat(clone.findOrCreateProtocolDialectConfigurationProperties(dialect2).getProperty(propertyName2)).isEqualTo(propertyValue2);
    }

    @Test
    @Transactional
    public void cloneWithSecuritySetsTest() {
        String securitySetName1 = "No security";
        String securitySetName2 = "Pentagon Security";
        final int accessLevelOne = 1;
        final int accessLevelPentagon = 9999;
        enhanceDeviceProtocolWithSecurityPropertySet(accessLevelOne, accessLevelPentagon);
        DeviceType deviceType = createSimpleDeviceType();
        DeviceConfiguration configWithSecuritySets = deviceType.newConfiguration("ConfigWithSecuritySets").add();
        configWithSecuritySets.createSecurityPropertySet(securitySetName1).authenticationLevel(accessLevelOne).encryptionLevel(accessLevelOne).build();
        configWithSecuritySets.createSecurityPropertySet(securitySetName2).authenticationLevel(accessLevelPentagon).encryptionLevel(accessLevelPentagon).build();
        configWithSecuritySets.save();

        DeviceConfiguration clone = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(configWithSecuritySets, "CloneWithSecuritySets");

        assertThat(configWithSecuritySets.getSecurityPropertySets()).hasSize(2);
        verifyBasicStuffForClonedConfigs(configWithSecuritySets, clone);
        assertThat(clone.getSecurityPropertySets()).hasSize(2);
        assertThat(clone.getSecurityPropertySets()).has(new Condition<List<SecurityPropertySet>>() {
            @Override
            public boolean matches(List<SecurityPropertySet> securityPropertySets) {
                boolean nameCheck = securityPropertySets.stream().filter(securityPropertySet -> securityPropertySet.getName().equals(securitySetName1) || securityPropertySet.getName().equals(securitySetName2)).count() == 2;
                boolean authenticationAccessLevelCheck = securityPropertySets.stream().filter(securityPropertySet -> securityPropertySet.getAuthenticationDeviceAccessLevel().getId() == accessLevelOne || securityPropertySet.getAuthenticationDeviceAccessLevel().getId() == accessLevelPentagon).count() == 2;
                boolean encryptionAccessLevelCheck = securityPropertySets.stream().filter(securityPropertySet -> securityPropertySet.getEncryptionDeviceAccessLevel().getId() == accessLevelOne || securityPropertySet.getEncryptionDeviceAccessLevel().getId() == accessLevelPentagon).count() == 2;
                return nameCheck && authenticationAccessLevelCheck && encryptionAccessLevelCheck;
            }
        });
    }

    private void enhanceDeviceProtocolWithSecurityPropertySet(int accessLevelOne, int accessLevelPentagon) {
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.<AuthenticationDeviceAccessLevel>asList(
                new AuthenticationDeviceAccessLevel() {
                    @Override
                    public int getId() {
                        return accessLevelOne;
                    }

                    @Override
                    public String getTranslationKey() {
                        return null;
                    }

                    @Override
                    public List<PropertySpec> getSecurityProperties() {
                        return Collections.emptyList();
                    }
                }, new AuthenticationDeviceAccessLevel() {
                    @Override
                    public int getId() {
                        return accessLevelPentagon;
                    }

                    @Override
                    public String getTranslationKey() {
                        return null;
                    }

                    @Override
                    public List<PropertySpec> getSecurityProperties() {
                        return Collections.emptyList();
                    }
                }));
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.<EncryptionDeviceAccessLevel>asList(
                new EncryptionDeviceAccessLevel() {
                    @Override
                    public int getId() {
                        return accessLevelOne;
                    }

                    @Override
                    public String getTranslationKey() {
                        return null;
                    }

                    @Override
                    public List<PropertySpec> getSecurityProperties() {
                        return Collections.emptyList();
                    }
                }, new EncryptionDeviceAccessLevel() {
                    @Override
                    public int getId() {
                        return accessLevelPentagon;
                    }

                    @Override
                    public String getTranslationKey() {
                        return null;
                    }

                    @Override
                    public List<PropertySpec> getSecurityProperties() {
                        return Collections.emptyList();
                    }
                }));    }

    private DeviceProtocolDialect mockDeviceProtocolDialect(String propertyName, String protocolDialectName, PropertySpec propertySpec) {
        DeviceProtocolDialect dialect1 = mock(DeviceProtocolDialect.class);
        when(dialect1.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(dialect1.getPropertySpec(propertyName)).thenReturn(propertySpec);
        when(dialect1.getDeviceProtocolDialectName()).thenReturn(protocolDialectName);
        return dialect1;
    }


    private void enhanceDeviceProtocolWithPropertySpecs(String propertyName1, String propertyName2) {
        PropertySpec propertySpec1 = inMemoryPersistence.getPropertySpecService().stringPropertySpec(propertyName1, false, null);
        PropertySpec propertySpec2 = inMemoryPersistence.getPropertySpecService().longPropertySpec(propertyName2, false, null);
        when(deviceProtocol.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec1, propertySpec2));
    }

    private void verifyBasicStuffForClonedConfigs(DeviceConfiguration original, DeviceConfiguration clone) {
        assertThat(original.getId()).isGreaterThan(0);
        assertThat(clone.getId()).isGreaterThan(0);
        assertThat(original.getId()).isNotEqualTo(clone.getId());
        assertThat(clone.isActive()).isFalse();
    }

    private DeviceType createSimpleDeviceType() {
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        deviceType.save();
        return deviceType;
    }

//    @Test
//    @Transactional
//    public void cloneWithConnectionMethodsTest() {
//        String connectionMethodName = "MyConnectionMethodName";
//        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
//        ConnectionType connectionType = mock(ConnectionType.class);
//        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.OUTBOUND);
//        when(connectionTypePluggableClass.getConnectionType()).thenReturn(connectionType);
//        enhanceDeviceProtocolWithDirectlyAddressableCapability();
//        DeviceType deviceType = createSimpleDeviceType();
//        DeviceConfiguration original = deviceType.newConfiguration("EmptyDeviceConfiguration").isDirectlyAddressable(true).add();
//        PartialScheduledConnectionTaskImpl scheduledConnectionTask = original.newPartialScheduledConnectionTask(connectionMethodName, connectionTypePluggableClass, TimeDuration.minutes(5), ConnectionStrategy.AS_SOON_AS_POSSIBLE).build();
//
//        DeviceConfiguration clone = inMemoryPersistence.getDeviceConfigurationService().cloneDeviceConfiguration(original, "ClonedEmptyDeviceConfiguration");
//
//        verifyBasicStuffForClonedConfigs(original, clone);
//        assertThat(clone.getPartialOutboundConnectionTasks()).hasSize(1);
//        assertThat(clone.getPartialOutboundConnectionTasks().get(0).getName()).isEqualTo(connectionMethodName);
//    }
}