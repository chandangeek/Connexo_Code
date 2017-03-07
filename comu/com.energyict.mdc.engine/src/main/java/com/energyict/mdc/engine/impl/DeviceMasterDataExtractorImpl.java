package com.energyict.mdc.engine.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Currying.use;

/**
 * Provides an implementation for the {@link DeviceMasterDataExtractor} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (13:19)
 */
@Component(name = "com.energyict.mdc.device.data.upl.master.data.extractor", service = {DeviceMasterDataExtractor.class}, immediate = true)
@SuppressWarnings("unused")
public class DeviceMasterDataExtractorImpl implements DeviceMasterDataExtractor {

    private volatile ServerEngineService engineService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;
    private volatile ProtocolPluggableService protocolPluggableService;

    @Activate
    public void activate() {
        Services.deviceMasterDataExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.deviceMasterDataExtractor(null);
    }

    @Reference
    public void setEngineService(ServerEngineService engineService) {
        this.engineService = engineService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Override
    public Optional<com.energyict.mdc.upl.meterdata.Device> find(long id) {
        return this.deviceService.findDeviceById(id).map(Device.class::cast);
    }

    @Override
    public long id(com.energyict.mdc.upl.meterdata.Device device) {
        return ((Device) device).getId();
    }

    @Override
    public String serialNumber(com.energyict.mdc.upl.meterdata.Device device) {
        return ((Device) device).getSerialNumber();
    }

    @Override
    public TimeZone timeZone(com.energyict.mdc.upl.meterdata.Device device) {
        return TimeZone.getTimeZone(((Device) device).getZone());
    }

    @Override
    public Optional<DeviceConfiguration> configuration(long id) {
        return this.deviceConfigurationService
                    .findDeviceConfiguration(id)
                    .map(use(DeviceConfigurationAdapter::new).with(this.protocolPluggableService));
    }

    @Override
    public DeviceConfiguration configuration(com.energyict.mdc.upl.meterdata.Device device) {
        return new DeviceConfigurationAdapter(((Device) device).getDeviceConfiguration(), this.protocolPluggableService);
    }

    @Override
    public Collection<CommunicationTask> enabledTasks(com.energyict.mdc.upl.meterdata.Device device) {
        return this.enabledTasks((Device) device);
    }

    private Collection<CommunicationTask> enabledTasks(Device device) {
        return device
                .getComTaskExecutions()
                .stream()
                .map(CommunicationTaskAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<SecurityPropertySet> securityPropertySets(com.energyict.mdc.upl.meterdata.Device device) {
        return this.securityPropertySets((Device) device);
    }

    private Collection<SecurityPropertySet> securityPropertySets(Device device) {
        return device
                .getDeviceConfiguration()
                .getSecurityPropertySets()
                .stream()
                .map(SecurityPropertySetAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public String protocolJavaClassName(com.energyict.mdc.upl.meterdata.Device device) {
        return ((Device) device)
                    .getDeviceProtocolPluggableClass()
                    .map(PluggableClass::getJavaClassName)
                    .orElse(null);
    }

    @Override
    public Collection<SecurityProperty> securityProperties(com.energyict.mdc.upl.meterdata.Device device, SecurityPropertySet securityPropertySet) {
        return this.securityProperties((Device) device, (SecurityPropertySetAdapter) securityPropertySet);
    }

    private Collection<SecurityProperty> securityProperties(Device device, SecurityPropertySetAdapter securityPropertySetAdapter) {
        return this.securityProperties(device, securityPropertySetAdapter.getActual());
    }

    private Collection<SecurityProperty> securityProperties(Device device, com.energyict.mdc.device.config.SecurityPropertySet securityPropertySet) {
        return device
                .getSecurityProperties(securityPropertySet)
                .stream()
                .map(SecurityPropertyAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public TypedProperties properties(com.energyict.mdc.upl.meterdata.Device device) {
        return this.properties((Device) device);
    }

    private TypedProperties properties(Device device) {
        return com.energyict.mdc.common.TypedProperties.empty();
    }

    @Override
    public TypedProperties protocolProperties(com.energyict.mdc.upl.meterdata.Device device) {
        return this.protocolProperties((Device) device);
    }

    private TypedProperties protocolProperties(Device device) {
        return device.getDeviceProtocolProperties();
    }


    @Override
    public Optional<TypedProperties> dialectProperties(com.energyict.mdc.upl.meterdata.Device device, String dialectName) {
        return this.dialectProperties((Device) device, dialectName);
    }

    private Optional<TypedProperties> dialectProperties(Device device, String dialectName) {
        return device
                .getProtocolDialectProperties(dialectName)
                .map(ProtocolDialectProperties::getTypedProperties);
    }

    @Override
    public List<com.energyict.mdc.upl.meterdata.Device> downstreamDevices(com.energyict.mdc.upl.meterdata.Device master) {
        return this.downstreamDevices((Device) master);
    }

    public List<com.energyict.mdc.upl.meterdata.Device> downstreamDevices(Device master) {
        return new ArrayList<>(this.topologyService.findPhysicalConnectedDevices(master));
    }

    @Override
    public Optional<com.energyict.mdc.upl.meterdata.Device> gateway(com.energyict.mdc.upl.meterdata.Device device) {
        return this.topologyService.getPhysicalGateway((Device) device).map(com.energyict.mdc.upl.meterdata.Device.class::cast);
    }

    private static class DeviceConfigurationAdapter implements DeviceConfiguration {
        private final ProtocolPluggableService protocolPluggableService;
        private final com.energyict.mdc.device.config.DeviceConfiguration actual;

        private DeviceConfigurationAdapter(com.energyict.mdc.device.config.DeviceConfiguration actual, ProtocolPluggableService protocolPluggableService) {
            this.actual = actual;
            this.protocolPluggableService = protocolPluggableService;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public String name() {
            return this.actual.getName();
        }

        @Override
        public String fullyQualifiedName(String separator) {
            return this.actual.getDeviceType().getName() + separator + this.actual.getName();
        }

        @Override
        public String protocolJavaClassName() {
            return this.actual
                        .getDeviceType()
                        .getDeviceProtocolPluggableClass()
                        .map(DeviceProtocolPluggableClass::getJavaClassName)
                        .orElse("");
        }

        @Override
        public TypedProperties properties() {
            return this.actual.getDeviceProtocolProperties().getTypedProperties();
        }

        @Override
        public Optional<TypedProperties> dialectProperties(String dialectName) {
            return this.actual
                    .getProtocolDialectConfigurationPropertiesList()
                    .stream()
                    .filter(each -> each.getDeviceProtocolDialectName().equals(dialectName))
                    .findAny()
                    .map(ProtocolDialectConfigurationProperties::getTypedProperties);
        }

        @Override
        public Collection<LogBookSpec> logBookSpecs() {
            return this.actual
                    .getLogBookSpecs()
                    .stream()
                    .map(LogBookSpecAdapter::new)
                    .collect(Collectors.toList());
        }

        @Override
        public Collection<LoadProfileSpec> loadProfileSpecs() {
            return this.actual
                    .getLoadProfileSpecs()
                    .stream()
                    .map(LoadProfileSpecAdapter::new)
                    .collect(Collectors.toList());
        }

        @Override
        public Collection<RegisterSpec> registerSpecs() {
            return this.actual
                    .getRegisterSpecs()
                    .stream()
                    .map(RegisterSpecAdapter::new)
                    .collect(Collectors.toList());
        }

        @Override
        public List<com.energyict.mdc.upl.meterdata.Device> devices() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DeviceConfigurationAdapter that = (DeviceConfigurationAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private class NextExecutionSpecsAdapter implements NextExecutionSpecs {
        private final com.energyict.mdc.scheduling.NextExecutionSpecs actual;

        private NextExecutionSpecsAdapter(com.energyict.mdc.scheduling.NextExecutionSpecs actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public String displayName() {
            return new NextExecutionSpecsFormat(engineService.thesaurus()).format(this.actual);
        }

        @Override
        public SchedulingSpecificationType type() {
            if (this.actual.getTemporalExpression() != null) {
                return SchedulingSpecificationType.TEMPORAL;
            } else {
                return SchedulingSpecificationType.DIAL_CALENDAR;
            }
        }

        @Override
        public String toCronExpression(TimeZone targetTimeZone, TimeZone definitionTimeZone) {
            return this.actual.getTemporalExpression().toCronExpression(targetTimeZone, definitionTimeZone);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NextExecutionSpecsAdapter that = (NextExecutionSpecsAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private class CommunicationTaskAdapter implements CommunicationTask {
        private final ComTaskExecution actual;

        private CommunicationTaskAdapter(ComTaskExecution actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public String name() {
            return this.actual.getComTask().getName();
        }

        @Override
        public SecurityPropertySet securityPropertySet() {
            /* ScheduledComTaskExecutions:
             *   1. have at least one ComTasks (so get(0) is not returning null)
             *   2. all ComTasks in the ComSchedule must use the same SecurityPropertySet
             * Therefore, it suffices to take the first ComTask. */
            ComTask anyComTask = this.actual.getComTasks().get(0);
            return this.actual
                        .getDevice()
                        .getDeviceConfiguration()
                        .getComTaskEnablementFor(anyComTask)
                        .map(ComTaskEnablement::getSecurityPropertySet)
                        .map(SecurityPropertySetAdapter::new)
                        .orElse(null);
        }

        @Override
        public Optional<NextExecutionSpecs> nextExecutionSpecs() {
            return this.actual
                        .getNextExecutionSpecs()
                        .map(NextExecutionSpecsAdapter::new);
        }

        @Override
        public List<ProtocolTask> protocolTasks() {
            return this.actual
                    .getProtocolTasks()
                    .stream()
                    .map(ProtocolTaskAdapterFactory::adapt)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isConfiguredToCollectLoadProfileData() {
            return this.actual.isConfiguredToCollectLoadProfileData();
        }

        @Override
        public boolean isConfiguredToCollectRegisterData() {
            return this.actual.isConfiguredToCollectRegisterData();
        }

        @Override
        public boolean isConfiguredToCollectEvents() {
            return this.actual.isConfiguredToCollectEvents();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CommunicationTaskAdapter that = (CommunicationTaskAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private class SecurityPropertySetAdapter implements SecurityPropertySet {
        private final com.energyict.mdc.device.config.SecurityPropertySet actual;

        private SecurityPropertySetAdapter(com.energyict.mdc.device.config.SecurityPropertySet actual) {
            this.actual = actual;
        }

        com.energyict.mdc.device.config.SecurityPropertySet getActual() {
            return actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public int authenticationDeviceAccessLevelId() {
            return this.actual.getAuthenticationDeviceAccessLevel().getId();
        }

        @Override
        public int encryptionDeviceAccessLevelId() {
            return this.actual.getEncryptionDeviceAccessLevel().getId();
        }

        @Override
        public Set<PropertySpec> propertySpecs() {
            return this.actual
                    .getPropertySpecs()
                    .stream()
                    .map(protocolPluggableService::adapt)
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SecurityPropertySetAdapter that = (SecurityPropertySetAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class LogBookSpecAdapter implements LogBookSpec {
        private final com.energyict.mdc.device.config.LogBookSpec actual;

        private LogBookSpecAdapter(com.energyict.mdc.device.config.LogBookSpec actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public ObisCode obisCode() {
            return this.actual.getObisCode();
        }

        @Override
        public ObisCode deviceObisCode() {
            return this.actual.getDeviceObisCode();
        }

        @Override
        public LogBookType type() {
            return new LogBookTypeAdapter(this.actual.getLogBookType());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LogBookSpecAdapter that = (LogBookSpecAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class LogBookTypeAdapter implements LogBookType {
        private final com.energyict.mdc.masterdata.LogBookType actual;

        private LogBookTypeAdapter(com.energyict.mdc.masterdata.LogBookType actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public String name() {
            return this.actual.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LogBookTypeAdapter that = (LogBookTypeAdapter) o;
            return Objects.equals(actual, that.actual);
        }

        @Override
        public int hashCode() {
            return Objects.hash(actual);
        }
    }

    private static class LoadProfileSpecAdapter implements LoadProfileSpec {
        private final com.energyict.mdc.device.config.LoadProfileSpec actual;

        private LoadProfileSpecAdapter(com.energyict.mdc.device.config.LoadProfileSpec actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public ObisCode obisCode() {
            return this.actual.getObisCode();
        }

        @Override
        public ObisCode deviceObisCode() {
            return this.actual.getDeviceObisCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LoadProfileSpecAdapter that = (LoadProfileSpecAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class RegisterSpecAdapter implements RegisterSpec {
        private final com.energyict.mdc.device.config.RegisterSpec actual;

        private RegisterSpecAdapter(com.energyict.mdc.device.config.RegisterSpec actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public ObisCode obisCode() {
            return this.actual.getObisCode();
        }

        @Override
        public ObisCode deviceObisCode() {
            return this.actual.getDeviceObisCode();
        }

        @Override
        public boolean contains(RegisterGroup group) {
            return this.actual
                        .getRegisterType()
                        .getRegisterGroups()
                        .stream()
                        .anyMatch(each -> each.getId() == group.id());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RegisterSpecAdapter that = (RegisterSpecAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class ProtocolTaskAdapterFactory {
        //Note that other tasks are not sync'ed to the Beacon
        static Optional<ProtocolTask> adapt(com.energyict.mdc.tasks.ProtocolTask cxoTask) {
            if (cxoTask instanceof ClockTask) {
                return Optional.of(new ClockTaskAdapter((ClockTask) cxoTask));
            } else if (cxoTask instanceof RegistersTask) {
                return Optional.of(new RegistersAdapter((RegistersTask) cxoTask));
            } else if (cxoTask instanceof LoadProfilesTask) {
                return Optional.of(new LoadProfilesAdapter((LoadProfilesTask) cxoTask));
            } else if (cxoTask instanceof LogBooksTask) {
                return Optional.of(new LogBooksAdapter((LogBooksTask) cxoTask));
            } else {
                return Optional.empty();
            }
        }
    }

    private static class ClockTaskAdapter implements Clock {
        private final ClockTask actual;

        private ClockTaskAdapter(ClockTask actual) {
            this.actual = actual;
        }

        @Override
        public Duration minimumClockDifference() {
            return Duration.ofSeconds(
                    this.actual
                        .getMinimumClockDifference()
                        .map(TimeDuration::getSeconds)
                        .orElse(0));
        }

        @Override
        public Duration maximumClockDifference() {
            return Duration.ofSeconds(
                    this.actual
                            .getMaximumClockDifference()
                            .map(TimeDuration::getSeconds)
                            .orElse(0));
        }

        @Override
        public Duration maximumClockShift() {
            return Duration.ofSeconds(
                    this.actual
                            .getMaximumClockShift()
                            .map(TimeDuration::getSeconds)
                            .orElse(0));
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClockTaskAdapter that = (ClockTaskAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class RegisterGroupAdapter implements RegisterGroup {
        private final com.energyict.mdc.masterdata.RegisterGroup actual;

        private RegisterGroupAdapter(com.energyict.mdc.masterdata.RegisterGroup actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RegisterGroupAdapter that = (RegisterGroupAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class RegistersAdapter implements Registers {
        private final RegistersTask actual;

        private RegistersAdapter(RegistersTask actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public Collection<RegisterGroup> groups() {
            return this.actual
                    .getRegisterGroups()
                    .stream()
                    .map(RegisterGroupAdapter::new)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RegistersAdapter that = (RegistersAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class LoadProfileTypeAdapter implements LoadProfileType {
        private final com.energyict.mdc.masterdata.LoadProfileType actual;

        private LoadProfileTypeAdapter(com.energyict.mdc.masterdata.LoadProfileType actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public String name() {
            return this.actual.getName();
        }

        @Override
        public ObisCode obisCode() {
            return this.actual.getObisCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LoadProfileTypeAdapter that = (LoadProfileTypeAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class LoadProfilesAdapter implements LoadProfiles {
        private final LoadProfilesTask actual;

        private LoadProfilesAdapter(LoadProfilesTask actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public Collection<LoadProfileType> types() {
            return this.actual
                    .getLoadProfileTypes()
                    .stream()
                    .map(LoadProfileTypeAdapter::new)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LoadProfilesAdapter that = (LoadProfilesAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class LogBooksAdapter implements LogBooks {
        private final LogBooksTask actual;

        private LogBooksAdapter(LogBooksTask actual) {
            this.actual = actual;
        }

        @Override
        public long id() {
            return this.actual.getId();
        }

        @Override
        public Collection<LogBookType> types() {
            return this.actual
                    .getLogBookTypes()
                    .stream()
                    .map(LogBookTypeAdapter::new)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LogBooksAdapter that = (LogBooksAdapter) o;
            return this.id() == that.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id());
        }
    }

    private static class SecurityPropertyAdapter implements SecurityProperty {
        private final com.energyict.mdc.protocol.api.security.SecurityProperty actual;

        private SecurityPropertyAdapter(com.energyict.mdc.protocol.api.security.SecurityProperty actual) {
            this.actual = actual;
        }

        @Override
        public String name() {
            return this.actual.getName();
        }

        @Override
        public Object value() {
            return this.actual.getValue();
        }
    }
}