/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.SecurityPropertyException;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SecurityPropertyServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-15 (09:010)
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityPropertyServiceImplTest {
    private static final long SECURITY_PROPERTY_SET1_ID = 97L;
    private static final long SECURITY_PROPERTY_SET2_ID = 103L;
    private static final long ETERNITY = 1_000_000_000_000_000_000L;
    private static final String USERNAME_SECURITY_PROPERTY_NAME = "username";
    private static final String PASSWORD_SECURITY_PROPERTY_NAME = "password";
    private static final String SOME_KEY_SECURITY_PROPERTY_NAME = "someKey";

    @Mock
    private PluggableService pluggableService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private SecurityPropertySet securityPropertySet1;
    @Mock
    private SecurityPropertySet securityPropertySet2;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private Device device;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private Clock clock;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    @Mock
    private ConnectionTypeService connectionTypeService;
    @Mock
    private LicensedProtocolService licensedProtocolService;
    @Mock
    private DeviceCacheMarshallingService deviceCacheMarshallingService;
    @Mock
    private NlsService nlsService;
    @Mock
    private TimeService timeService;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;

    @Before
    public void initializeMocks () {
        when(clock.instant()).thenReturn(Instant.now());
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));
        ComTaskEnablement cte1 = mock(ComTaskEnablement.class);
        when(cte1.getSecurityPropertySet()).thenReturn(this.securityPropertySet1);
        ComTaskEnablement cte2 = mock(ComTaskEnablement.class);
        when(cte2.getSecurityPropertySet()).thenReturn(this.securityPropertySet2);
        when(this.deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(cte1, cte2));
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(this.deviceProtocolPluggableClass));
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>> customPropertySet = TestProtocolWithOnlySecurityProperties.getCustomPropertySet(
                new PropertySpecServiceImpl(
                        new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(this.timeService, this.ormService, new DefaultBeanService()),
                        this.dataVaultService,
                        this.ormService));
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));
        when(this.pluggableService
                .newPluggableClass(PluggableClassType.DeviceProtocol, "SecurityPropertyServiceImplTest", TestProtocolWithOnlySecurityProperties.class.getName()))
                .thenReturn(this.deviceProtocolPluggableClass);
        when(this.protocolPluggableService.isLicensedProtocolClassName(anyString())).thenReturn(true);

        when(this.securityPropertySet1.getId()).thenReturn(SECURITY_PROPERTY_SET1_ID);
        PropertySpec userName = mock(PropertySpec.class);
        when(userName.getName()).thenReturn(USERNAME_SECURITY_PROPERTY_NAME);
        when(userName.isRequired()).thenReturn(true);
        when(userName.getValueFactory()).thenReturn(new StringFactory());
        PropertySpec password = mock(PropertySpec.class);
        when(password.getName()).thenReturn(PASSWORD_SECURITY_PROPERTY_NAME);
        when(password.isRequired()).thenReturn(true);
        when(password.getValueFactory()).thenReturn(new StringFactory());
        when(this.securityPropertySet1.getPropertySpecs()).thenReturn(new HashSet<>(Arrays.asList(userName, password)));
        when(this.securityPropertySet2.getId()).thenReturn(SECURITY_PROPERTY_SET2_ID);
        PropertySpec someKey = mock(PropertySpec.class);
        when(someKey.getName()).thenReturn(SOME_KEY_SECURITY_PROPERTY_NAME);
        when(someKey.getValueFactory()).thenReturn(new StringFactory());
        when(someKey.isRequired()).thenReturn(true);
        when(this.securityPropertySet2.getPropertySpecs()).thenReturn(new HashSet<>(Collections.singletonList(someKey)));
        when(this.customPropertySetService
                .getUniqueValuesFor(
                        any(CustomPropertySet.class),
                        any(),
                        any(Instant.class),
                        any(SecurityPropertySet.class)))
                .thenReturn(CustomPropertySetValues.empty());
    }

    @Test
    public void hasSecurityPropertiesForUserThatIsNotAllowedToView () {
        Instant effectiveStart = Instant.ofEpochSecond(97L);
        Instant effectiveEnd = effectiveStart.plusSeconds(10);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));
        when(this.customPropertySetService
                .getUniqueValuesFor(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(this.securityPropertySet1)))
                .thenReturn(CustomPropertySetValues.emptyDuring(Range.closedOpen(effectiveStart, effectiveEnd)));

        // Business method
        boolean hasSecurityProperties = this.testService().hasSecurityProperties(this.device, effectiveStart.plusSeconds(1), this.securityPropertySet1);

        // Asserts
        assertThat(hasSecurityProperties).isTrue();
    }

    @Test
    public void hasSecurityPropertiesForUserThatIsAllowedToView () {
        Instant effectiveStart = Instant.ofEpochSecond(97L);
        Instant effectiveEnd = effectiveStart.plusSeconds(10);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));
        when(this.customPropertySetService
                .getUniqueValuesFor(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(this.securityPropertySet1)))
                .thenReturn(CustomPropertySetValues.emptyDuring(Range.closedOpen(effectiveStart, effectiveEnd)));

        // Business method
        boolean hasSecurityProperties = this.testService().hasSecurityProperties(this.device, effectiveStart.plusSeconds(1), this.securityPropertySet1);

        // Asserts
        assertThat(hasSecurityProperties).isTrue();
    }

    @Test
    public void getSecurityPropertiesWithoutCustomPropertySet() {
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.empty());

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, Instant.now(), this.securityPropertySet1);

        // Asserts
        verify(this.deviceProtocol).getCustomPropertySet();
        assertThat(securityProperties).isEmpty();
    }

    @Test
    public void getSecurityProperties() throws SQLException {
        Instant effectiveValueIntervalStart = Instant.ofEpochSecond(97L);
        Instant effectiveValueIntervalEnd = effectiveValueIntervalStart.plusSeconds(100);
        String expectedUser = "current user";
        String expectedPassword = "current password";
        when(this.clock.instant()).thenReturn(effectiveValueIntervalStart.plusSeconds(10));
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.emptyDuring(Ranges.closedOpen(effectiveValueIntervalStart, effectiveValueIntervalEnd));
        customPropertySetValues.setProperty(USERNAME_SECURITY_PROPERTY_NAME, expectedUser);
        customPropertySetValues.setProperty(PASSWORD_SECURITY_PROPERTY_NAME, expectedPassword);
        when(this.customPropertySetService
                .getUniqueValuesFor(customPropertySet, device, this.clock.instant(), this.securityPropertySet1))
                .thenReturn(customPropertySetValues);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, this.clock.instant(), this.securityPropertySet1);

        // Asserts
        verify(this.customPropertySetService).getUniqueValuesFor(customPropertySet, device, this.clock.instant(), this.securityPropertySet1);
        Map<String, Object> propertyNames =
                securityProperties
                        .stream()
                        .collect(Collectors.toMap(
                                SecurityProperty::getName,
                                SecurityProperty::getValue));
        assertThat(propertyNames.keySet()).containsOnly(USERNAME_SECURITY_PROPERTY_NAME, PASSWORD_SECURITY_PROPERTY_NAME);
        assertThat(propertyNames.get(USERNAME_SECURITY_PROPERTY_NAME)).isEqualTo(expectedUser);
        assertThat(propertyNames.get(PASSWORD_SECURITY_PROPERTY_NAME)).isEqualTo(expectedPassword);
    }

    @Test
    public void getSecurityPropertiesWithoutValues() throws SQLException {
        Instant effectiveValueIntervalStart = Instant.ofEpochSecond(97L);
        Instant effectiveValueIntervalEnd = effectiveValueIntervalStart.plusSeconds(100);
        when(this.clock.instant()).thenReturn(effectiveValueIntervalStart.plusSeconds(10));
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.emptyDuring(Ranges.closedOpen(effectiveValueIntervalStart, effectiveValueIntervalEnd));
        when(this.customPropertySetService
                .getUniqueValuesFor(customPropertySet, device, this.clock.instant(), this.securityPropertySet1))
                .thenReturn(customPropertySetValues);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, this.clock.instant(), this.securityPropertySet1);

        // Asserts
        verify(this.customPropertySetService).getUniqueValuesFor(customPropertySet, device, this.clock.instant(), this.securityPropertySet1);
        assertThat(securityProperties).isEmpty();
    }

//    @Test(expected = SecurityPropertyException.class)
//    public void setSecurityPropertiesForUserThatIsNotAllowedToEdit () {
//        when(this.securityPropertySet1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(false);
//
//        // Business method
//        this.testService().setSecurityProperties(this.device, this.securityPropertySet1, TypedProperties.empty());
//
//        // Asserts
//        verify(this.securityPropertySet1).currentUserIsAllowedToEditDeviceProperties();
//    }

    @Test
    public void setSecurityProperties () throws SQLException {
        Instant now = Instant.ofEpochSecond(97L);
        when(this.clock.instant()).thenReturn(now);
        TypedProperties properties = TypedProperties.empty();
        BigDecimal expectedValueForPropertyOne = BigDecimal.ONE;
        properties.setProperty("One", expectedValueForPropertyOne);
        String expectedValueForPropertyTwo = "Due";
        properties.setProperty("Two", expectedValueForPropertyTwo);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));

        // Business method
        this.testService().setSecurityProperties(this.device, this.securityPropertySet1, properties);

        // Asserts
        ArgumentCaptor<CustomPropertySetValues> customPropertySetValuesCaptor = ArgumentCaptor.forClass(CustomPropertySetValues.class);
        verify(this.customPropertySetService).setValuesFor(eq(customPropertySet), eq(this.device), customPropertySetValuesCaptor.capture(), eq(now), eq(this.securityPropertySet1));
        CustomPropertySetValues customPropertySetValues = customPropertySetValuesCaptor.getValue();
        assertThat(customPropertySetValues).isNotNull();
        assertThat(customPropertySetValues.getEffectiveRange()).isEqualTo(Range.atLeast(now));
        assertThat(customPropertySetValues.getProperty(CommonBaseDeviceSecurityProperties.Fields.PROPERTY_SPEC_PROVIDER.javaName())).isEqualTo(this.securityPropertySet1);
        assertThat(customPropertySetValues.getProperty("One")).isEqualTo(expectedValueForPropertyOne);
        assertThat(customPropertySetValues.getProperty("Two")).isEqualTo(expectedValueForPropertyTwo);
    }

    @Test
    public void securityPropertiesAreNotValidWhenAllAreMissing() {
        SecurityPropertyService service = this.testService();
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(this.customPropertySetService
            .getUniqueValuesFor(
                eq(customPropertySet),
                eq(this.device),
                any(Instant.class),
                eq(this.securityPropertySet1)))
            .thenReturn(CustomPropertySetValues.empty());
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreNotValidWhenOneIsMissing() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        SecurityPropertyService service = this.testService();
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.emptyDuring(Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY)));
        customPropertySetValues.setProperty(USERNAME_SECURITY_PROPERTY_NAME, "test");
        customPropertySetValues.setProperty(PASSWORD_SECURITY_PROPERTY_NAME, "pass");
        customPropertySetValues.setProperty(CommonBaseDeviceSecurityProperties.Fields.COMPLETE.javaName(), true);
        when(this.customPropertySetService
                .getUniqueValuesFor(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(this.securityPropertySet1)))
                .thenReturn(customPropertySetValues);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreValidWhenUnusedOneIsMissing() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        SecurityPropertySet unused = mock(SecurityPropertySet.class);
        when(unused.getId()).thenReturn(SECURITY_PROPERTY_SET2_ID + 1);
        PropertySpec otherKey = mock(PropertySpec.class);
        when(otherKey.getName()).thenReturn("Other");
        when(otherKey.getValueFactory()).thenReturn(new StringFactory());
        when(otherKey.isRequired()).thenReturn(true);
        when(unused.getPropertySpecs()).thenReturn(new HashSet<>(Collections.singletonList(otherKey)));
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));

        PropertySpec someKey = mock(PropertySpec.class);
        when(someKey.getName()).thenReturn(SOME_KEY_SECURITY_PROPERTY_NAME);
        when(someKey.getValueFactory()).thenReturn(new StringFactory());
        when(someKey.isRequired()).thenReturn(false);

        Set<PropertySpec> requiredPropertySpecs = securityPropertySet1.getPropertySpecs();
        when(this.securityPropertySet1.getPropertySpecs()).thenReturn(Stream.concat(requiredPropertySpecs.stream(), Stream.of(someKey)).collect(Collectors.toSet()));
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(this.customPropertySetService
                .hasValueForPropertySpecs(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(requiredPropertySpecs),
                        eq(this.securityPropertySet1)))
                .thenReturn(true);
        requiredPropertySpecs = this.securityPropertySet2.getPropertySpecs();
        when(this.securityPropertySet2.getPropertySpecs()).thenReturn(Stream.concat(requiredPropertySpecs.stream(), Stream.of(someKey)).collect(Collectors.toSet()));
        when(this.customPropertySetService
                .hasValueForPropertySpecs(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(requiredPropertySpecs),
                        eq(this.securityPropertySet2)))
                .thenReturn(true);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));

        SecurityPropertyService service = this.testService();

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isTrue();
    }

    @Test
    public void securityPropertiesAreNotValidWhenAllAreInComplete() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        SecurityPropertyService service = this.testService();
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        List<PropertySpec> usedSpecs = new ArrayList<>();
        usedSpecs.addAll(this.securityPropertySet1.getPropertySpecs());
        usedSpecs.addAll(this.securityPropertySet2.getPropertySpecs());
        when(customPropertySet.getPropertySpecs()).thenReturn(usedSpecs);
        CustomPropertySetValues customPropertySetValuesForSet1 = CustomPropertySetValues.emptyDuring(Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY)));
        customPropertySetValuesForSet1.setProperty(USERNAME_SECURITY_PROPERTY_NAME, "test");
        when(this.customPropertySetService
                .getUniqueValuesFor(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(this.securityPropertySet1)))
                .thenReturn(customPropertySetValuesForSet1);
        CustomPropertySetValues customPropertySetValuesForSet2 = CustomPropertySetValues.emptyDuring(Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY)));
        when(this.customPropertySetService
                .getUniqueValuesFor(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(this.securityPropertySet1)))
                .thenReturn(customPropertySetValuesForSet2);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreNotValidWhenSomeAreInComplete() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        SecurityPropertyService service = this.testService();
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        List<PropertySpec> usedSpecs = new ArrayList<>();
        usedSpecs.addAll(this.securityPropertySet1.getPropertySpecs());
        usedSpecs.addAll(this.securityPropertySet2.getPropertySpecs());
        when(customPropertySet.getPropertySpecs()).thenReturn(usedSpecs);
        CustomPropertySetValues customPropertySetValuesForSet1 = CustomPropertySetValues.emptyDuring(Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY)));
        customPropertySetValuesForSet1.setProperty(USERNAME_SECURITY_PROPERTY_NAME, "test");
        customPropertySetValuesForSet1.setProperty(PASSWORD_SECURITY_PROPERTY_NAME, "pass");
        when(this.customPropertySetService
                .getUniqueValuesFor(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(this.securityPropertySet1)))
                .thenReturn(customPropertySetValuesForSet1);
        CustomPropertySetValues customPropertySetValuesForSet2 = CustomPropertySetValues.emptyDuring(Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY)));
        when(this.customPropertySetService
                .getUniqueValuesFor(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(this.securityPropertySet1)))
                .thenReturn(customPropertySetValuesForSet2);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreValid() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        SecurityPropertyService service = this.testService();
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        List<PropertySpec> usedSpecs = new ArrayList<>();
        usedSpecs.addAll(this.securityPropertySet1.getPropertySpecs());
        usedSpecs.addAll(this.securityPropertySet2.getPropertySpecs());
        when(customPropertySet.getPropertySpecs()).thenReturn(usedSpecs);
        Set<PropertySpec> requiredPropertySpecs = securityPropertySet1.getPropertySpecs();
        when(this.customPropertySetService
                .hasValueForPropertySpecs(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(requiredPropertySpecs),
                        eq(this.securityPropertySet1)))
                .thenReturn(true);
        requiredPropertySpecs = securityPropertySet2.getPropertySpecs();
        when(this.customPropertySetService
                .hasValueForPropertySpecs(
                        eq(customPropertySet),
                        eq(this.device),
                        any(Instant.class),
                        eq(requiredPropertySpecs),
                        eq(this.securityPropertySet2)))
                .thenReturn(true);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isTrue();
    }

    @Test
    public void deleteSecurityProperties() throws SQLException {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        SecurityPropertyService service = this.testService();
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(this.deviceProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));

        // Business method
        service.deleteSecurityPropertiesFor(this.device);

        // Asserts
        verify(this.customPropertySetService).removeValuesFor(customPropertySet, this.device, this.securityPropertySet1);
        verify(this.customPropertySetService).removeValuesFor(customPropertySet, this.device, this.securityPropertySet2);
    }

    private SecurityPropertyService testService () {
        return new SecurityPropertyServiceImpl(this.clock, this.customPropertySetService, this.nlsService);
    }

}