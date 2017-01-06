package com.energyict.mdc.upl;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.tasks.TopologyAction;

import com.energyict.obis.ObisCode;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

/**
 * Extracts master data information that pertains to {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-05 (11:08)
 */
public interface DeviceMasterDataExtractor {

    Optional<Device> find(long id);

    long id(Device device);

    String serialNumber(Device device);

    TimeZone timeZone(Device device);

    Optional<DeviceConfiguration> configuration(long id);

    DeviceConfiguration configuration(Device device);

    Collection<SecurityPropertySet> securityPropertySets(Device device);

    /**
     * Extracts the {@link Device}s that use the master device as a gateway.
     */
    List<Device> downstreamDevices(Device master);

    TypedProperties properties(Device device);

    Collection<SecurityProperty> securityProperties(Device device, SecurityPropertySet securityPropertySet);

    Optional<Device> gateway(Device device);

    String protocolJavaClassName(Device device);

    interface DeviceConfiguration {
        long id();
        String name();
        String fullyQualifiedName(String separator);
        String protocolJavaClassName();
        TypedProperties properties();
        Optional<TypedProperties> dialectProperties(String dialectName);
        Collection<CommunicationTask> enabledTasks();
        Collection<LogBookSpec> logBookSpecs();
        Collection<LoadProfileSpec> loadProfileSpecs();
        Collection<RegisterSpec> registerSpecs();
        List<Device> devices();
    }

    interface SecurityPropertySet {
        long id();
        int authenticationDeviceAccessLevelId();
        int encryptionDeviceAccessLevelId();
        Set<PropertySpec> propertySpecs();
    }

    interface SecurityProperty {
        String name();
        Object value();
    }

    interface LogBookSpec {
        long id();
        ObisCode obisCode();
        ObisCode deviceObisCode();
        LogBookType type();
    }

    interface LoadProfileSpec {
        long id();
        ObisCode obisCode();
        ObisCode deviceObisCode();
    }

    interface RegisterSpec {
        long id();
        ObisCode obisCode();
        ObisCode deviceObisCode();
        boolean contains(RegisterGroup group);
    }

    enum SchedulingSpecificationType {
        TEMPORAL, DIAL_CALENDAR;
    }

    interface NextExecutionSpecs {
        long id();
        String displayName();
        SchedulingSpecificationType type();
        String toCronExpression(TimeZone targetTimeZone, TimeZone definitionTimeZone);
    }

    interface CommunicationTask {
        long id();
        String name();
        SecurityPropertySet securityPropertySet();
        Optional<NextExecutionSpecs> nextExecutionSpecs();
        Collection<ProtocolTask> protocolTasks();
        boolean isConfiguredToCollectLoadProfileData();
        boolean isConfiguredToCollectRegisterData();
        boolean isConfiguredToCollectEvents();
    }

    interface ProtocolTask {
        long id();
    }

    interface Messages extends ProtocolTask {
        boolean isAllCategories();
        Set<DeviceMessageCategory> categories();
        Set<DeviceMessageSpec> specs();
    }

    interface RegisterGroup {
        long id();
    }

    interface Registers extends ProtocolTask {
        Collection<RegisterGroup> groups();
    }

    interface BasicCheck extends ProtocolTask {
        boolean verifiesClockDifference();
        boolean verifiesSerialNumber();
        Duration maximumClockDifference();
    }

    interface Topology extends ProtocolTask {
        TopologyAction action();
    }

    interface Clock extends ProtocolTask {
        Duration minimumClockDifference();
        Duration maximumClockDifference();
        Duration maximumClockShift();
    }

    interface StatusInformation extends ProtocolTask {
    }

    interface LogBookType {
        long id();
        String name();
    }

    interface LogBooks extends ProtocolTask {
        Collection<LogBookType> types();
    }

    interface LoadProfileType {
        long id();
        String name();
        ObisCode obisCode();
    }

    interface LoadProfiles extends ProtocolTask {
        Collection<LoadProfileType> types();
    }

    interface ManualMeterReadings extends ProtocolTask {
        Collection<RegisterGroup> groups();
    }

}