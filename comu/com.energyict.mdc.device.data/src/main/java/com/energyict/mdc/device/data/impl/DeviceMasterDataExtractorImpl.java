package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ClockTask;
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
import org.osgi.service.component.annotations.Reference;

import java.time.Duration;
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
@Component(name = "com.energyict.mdc.device.data.upl.master.data.extractor", service = {DeviceMasterDataExtractor.class})
@SuppressWarnings("unused")
public class DeviceMasterDataExtractorImpl implements DeviceMasterDataExtractor {

    private volatile ServerDeviceService deviceService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;

    @Activate
    public void activate() {
        Services.deviceMasterDataExtractor(this);
    }

    @Reference
    public void setDeviceService(ServerDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
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
        return null;
    }

    @Override
    public String protocolJavaClassName(com.energyict.mdc.upl.meterdata.Device device) {
        return null;
    }

    @Override
    public Collection<SecurityPropertySet> securityPropertySets(com.energyict.mdc.upl.meterdata.Device device) {
        return null;
    }

    @Override
    public Collection<SecurityProperty> securityProperties(com.energyict.mdc.upl.meterdata.Device device, SecurityPropertySet securityPropertySet) {
        return null;
    }

    @Override
    public TypedProperties properties(com.energyict.mdc.upl.meterdata.Device device) {
        return null;
    }

    @Override
    public TypedProperties protocolProperties(com.energyict.mdc.upl.meterdata.Device device) {
        return null;
    }

    @Override
    public Optional<TypedProperties> dialectProperties(com.energyict.mdc.upl.meterdata.Device device, String dialectName) {
        return null;
    }

    @Override
    public List<com.energyict.mdc.upl.meterdata.Device> downstreamDevices(com.energyict.mdc.upl.meterdata.Device master) {
        return null;
    }

    @Override
    public Optional<com.energyict.mdc.upl.meterdata.Device> gateway(com.energyict.mdc.upl.meterdata.Device device) {
        return null;
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
            /* Both device configuration and pluggable classes do not have
             * any properties in Connexo yet. */
            return com.energyict.mdc.common.TypedProperties.empty();
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
        public Collection<CommunicationTask> enabledTasks() {
            return this.actual
                    .getComTaskEnablements()
                    .stream()
                    .map(use(CommunicationTaskAdapter::new).with(this.protocolPluggableService))
                    .collect(Collectors.toList());
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

    private static class CommunicationTaskAdapter implements CommunicationTask {
        private final ComTaskEnablement actual;
        private final ProtocolPluggableService protocolPluggableService;

        private CommunicationTaskAdapter(ComTaskEnablement actual, ProtocolPluggableService protocolPluggableService) {
            this.actual = actual;
            this.protocolPluggableService = protocolPluggableService;
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
            return new SecurityPropertySetAdapter(this.actual.getSecurityPropertySet(), this.protocolPluggableService);
        }

        @Override
        public Optional<NextExecutionSpecs> nextExecutionSpecs() {
            return Optional.ofNullable(this.actual.getNextExecutionSpecs()).map(NextExecutionSpecsAdapter::new);
        }

        @Override
        public List<ProtocolTask> protocolTasks() {
            return this.actual
                    .getComTask()
                    .getProtocolTasks()
                    .stream()
                    .map(ProtocolTaskAdapterFactory::adapt)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isConfiguredToCollectLoadProfileData() {
            return this.actual.getComTask().isConfiguredToCollectLoadProfileData();
        }

        @Override
        public boolean isConfiguredToCollectRegisterData() {
            return this.actual.getComTask().isConfiguredToCollectRegisterData();
        }

        @Override
        public boolean isConfiguredToCollectEvents() {
            return this.actual.getComTask().isConfiguredToCollectEvents();
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

    private static class SecurityPropertySetAdapter implements SecurityPropertySet {
        private final ProtocolPluggableService protocolPluggableService;
        private final com.energyict.mdc.device.config.SecurityPropertySet actual;

        private SecurityPropertySetAdapter(com.energyict.mdc.device.config.SecurityPropertySet actual, ProtocolPluggableService protocolPluggableService) {
            this.actual = actual;
            this.protocolPluggableService = protocolPluggableService;
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
                    .map(this.protocolPluggableService::adapt)
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
        static ProtocolTask adapt(com.energyict.mdc.tasks.ProtocolTask mdwTask) {
            if (mdwTask instanceof ClockTask) {
                return new ClockTaskAdapter((ClockTask) mdwTask);
            } else if (mdwTask instanceof RegistersTask) {
                return new RegistersAdapter((RegistersTask) mdwTask);
            } else if (mdwTask instanceof LoadProfilesTask) {
                return new LoadProfilesAdapter((LoadProfilesTask) mdwTask);
            } else if (mdwTask instanceof LogBooksTask) {
                return new LogBooksAdapter((LogBooksTask) mdwTask);
            } else {
                throw new IllegalArgumentException("ProtocolTask type not supported yet: " + mdwTask.getClass().getName());
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

}