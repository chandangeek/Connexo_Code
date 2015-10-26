package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.LocalizedTimeDuration;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.google.common.collect.Sets;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("basic functionality for load profiles")
public class BaseLoadProfileTest extends DeviceConfigurationApplicationJerseyTest {

    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;

    protected List<LoadProfileType> getLoadProfileTypes(int count) {
        List<LoadProfileType> loadProfileTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            TimeDuration randomTimeDuration = getRandomTimeDuration();
            loadProfileTypes.add(mockLoadProfileType(1000 + i, String.format("Load Profile Type %04d", i), randomTimeDuration,
                    new ObisCode(i, i, i, i, i, i), getChannelTypes(getRandomInt(4), randomTimeDuration)));
        }
        return loadProfileTypes;
    }

    protected List<LoadProfileSpec> getLoadProfileSpecs(int count) {
        List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            loadProfileSpecs.add(mockLoadProfileSpec(1000 + i));
        }
        return loadProfileSpecs;
    }


    protected List<ChannelType> getChannelTypes(int count, TimeDuration interval) {
        List<ChannelType> channelTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            channelTypes.add(mockChannelType(1000 + i, String.format("Channel type %04d", i), new ObisCode(i, i, i, i, i, i), interval));
        }
        return channelTypes;
    }


    protected int getRandomInt(int end) {
        return getRandomInt(0, end);
    }

    protected int getRandomInt(int start, int end) {
        int range = end - start;
        return (int) (start + new Random().nextDouble() * range);
    }

    protected TimeDuration getRandomTimeDuration() {
        return LocalizedTimeDuration.intervals.get(getRandomInt(LocalizedTimeDuration.intervals.size() - 1)).getTimeDuration();
    }

    protected DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mockRegisteredCustomPropertySet();
        when(deviceType.getLoadProfileTypeCustomPropertySet(anyObject())).thenReturn(Optional.of(registeredCustomPropertySet));
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getVersion()).thenReturn(OK_VERSION);
        return deviceType;
    }

    protected DeviceType mockDeviceType(String name, long id, List<PropertySpec> specs) {
        DeviceType deviceType = mock(DeviceType.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mockRegisteredCustomPropertySet();
        when(deviceType.getLoadProfileTypeCustomPropertySet(anyObject())).thenReturn(Optional.of(registeredCustomPropertySet));
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getPropertySpecs()).thenReturn(specs);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getId()).thenReturn(7L);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol pluggeable class");
        return deviceType;
    }

    protected DeviceConfiguration mockDeviceConfiguration(long id){
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn("Device configuration " + id);
        when(deviceConfiguration.getId()).thenReturn(id);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerType.getId()).thenReturn(101L);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.asList(registerSpec));
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findDeviceConfiguration(id)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        return deviceConfiguration;
    }

    protected LoadProfileType mockLoadProfileType(long id, String name, TimeDuration interval, ObisCode obisCode, List<ChannelType> channelTypes) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(id);
        when(loadProfileType.getName()).thenReturn(name);
        when(loadProfileType.getInterval()).thenReturn(interval);
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        when(loadProfileType.getChannelTypes()).thenReturn(channelTypes);
        when(loadProfileType.getVersion()).thenReturn(OK_VERSION);
        return loadProfileType;
    }

    protected RegisterType mockRegisterType(long id, String name, ObisCode obisCode) {
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(id);
        when(registerType.getObisCode()).thenReturn(obisCode);
        when(registerType.getTimeOfUse()).thenReturn(0);
        when(registerType.getUnit()).thenReturn(Unit.get("kWh"));
        ReadingType readingType = mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72." + id);
        when(readingType.getAliasName()).thenReturn(name);
        when(registerType.getReadingType()).thenReturn(readingType);
        return registerType;
    }

    protected ChannelType mockChannelType(long id, String name, ObisCode obisCode, TimeDuration interval) {
        ChannelType channelType = mock(ChannelType.class);
        when(channelType.getId()).thenReturn(id);
        when(channelType.getObisCode()).thenReturn(obisCode);
        when(channelType.getTimeOfUse()).thenReturn(0);
        when(channelType.getUnit()).thenReturn(Unit.get("kWh"));
        ReadingType readingType = mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72." + id);
        when(readingType.getAliasName()).thenReturn(name);
        when(channelType.getReadingType()).thenReturn(readingType);
        when(channelType.getInterval()).thenReturn(interval);
        RegisterType templateRegister = mockRegisterType(id, name, obisCode);
        when(channelType.getTemplateRegister()).thenReturn(templateRegister);
        return channelType;
    }

    protected LoadProfileSpec mockLoadProfileSpec(long id){
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        ObisCode obisCode = new ObisCode(0, 1, 2, 3, 4, 5);
        ObisCode overruledObisCode = new ObisCode(200,201,202,203,204,205);
        TimeDuration randomTimeDuration = getRandomTimeDuration();
        LoadProfileType loadProfileType = mockLoadProfileType(id, "Load profile spec " + id, randomTimeDuration, obisCode, getChannelTypes(2, randomTimeDuration));
        when(loadProfileSpec.getId()).thenReturn(id);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        when(loadProfileSpec.getObisCode()).thenReturn(obisCode);
        when(loadProfileSpec.getDeviceObisCode()).thenReturn(overruledObisCode);
        when(loadProfileSpec.getInterval()).thenReturn(getRandomTimeDuration());
        when(loadProfileSpec.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findLoadProfileSpec(id)).thenReturn(Optional.of(loadProfileSpec));
        when(deviceConfigurationService.findAndLockLoadProfileSpecByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(loadProfileSpec));
        when(deviceConfigurationService.findAndLockLoadProfileSpecByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        return loadProfileSpec;
    }

    protected ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        return readingType;
    }

    @SuppressWarnings("unchecked")
    private PropertySpec mockPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(BigDecimalFactory.class);
        when(propertySpec.getName()).thenReturn("customAttribute");
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.getValueFactory().getValueType()).thenReturn(BigDecimalFactory.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getDescription()).thenReturn("kw");
        return propertySpec;
    }

    @SuppressWarnings("unchecked")
    private CustomPropertySet mockCustomPropertySet() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getName()).thenReturn("domainExtensionName");
        when(customPropertySet.isRequired()).thenReturn(true);
        when(customPropertySet.isVersioned()).thenReturn(false);
        when(customPropertySet.defaultViewPrivileges()).thenReturn(Sets.newHashSet(ViewPrivilege.LEVEL_3));
        when(customPropertySet.defaultEditPrivileges()).thenReturn(Sets.newHashSet(EditPrivilege.LEVEL_4));
        when(customPropertySet.getDomainClass()).thenReturn(BigDecimalFactory.class);
        return customPropertySet;
    }

    @SuppressWarnings("unchecked")
    private RegisteredCustomPropertySet mockRegisteredCustomPropertySet() {
        PropertySpec propertySpec = mockPropertySpec();
        CustomPropertySet customPropertySet = mockCustomPropertySet();
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getId()).thenReturn(100500L);
        when(registeredCustomPropertySet.getViewPrivileges()).thenReturn(Sets.newHashSet(ViewPrivilege.LEVEL_1));
        when(registeredCustomPropertySet.getEditPrivileges()).thenReturn(Sets.newHashSet(EditPrivilege.LEVEL_2));
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        return registeredCustomPropertySet;
    }
}