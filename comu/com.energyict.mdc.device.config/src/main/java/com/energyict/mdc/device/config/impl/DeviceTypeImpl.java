package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.LogBookTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.RegisterTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.google.common.collect.ImmutableList;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@ProtocolCannotChangeWithExistingConfigurations(groups = {Save.Update.class})
public class DeviceTypeImpl extends PersistentNamedObject<DeviceType> implements DeviceType {

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    private String name;
    @Size(max = 4000, groups = {Save.Update.class, Save.Create.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;
    private boolean useChannelJournal;
    private int deviceUsageTypeId;
    private DeviceUsageType deviceUsageType;
    @Valid
    private List<DeviceConfiguration> deviceConfigurations = new ArrayList<>();
    private List<DeviceTypeLogBookTypeUsage> logBookTypeUsages = new ArrayList<>();
    private List<DeviceTypeLoadProfileTypeUsage> loadProfileTypeUsages = new ArrayList<>();
    private List<DeviceTypeRegisterTypeUsage> registerTypeUsages = new ArrayList<>();
    private long deviceProtocolPluggableClassId;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_PROTOCOL_IS_REQUIRED + "}")
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private boolean deviceProtocolPluggableClassChanged = false;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    private ProtocolPluggableService protocolPluggableService;
    private DeviceConfigurationService deviceConfigurationService;

    /**
     * The DeviceProtocol of this DeviceType, only for local usage
     */
    private DeviceProtocol localDeviceProtocol;

    @Inject
    public DeviceTypeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService) {
        super(DeviceType.class, dataModel, eventService, thesaurus);
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    DeviceTypeImpl initialize(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.setName(name);
        this.setDeviceProtocolPluggableClass(deviceProtocolPluggableClass);
        return this;
    }

    static DeviceTypeImpl from(DataModel dataModel, String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        return dataModel.getInstance(DeviceTypeImpl.class).initialize(name, deviceProtocolPluggableClass);
    }

    DeviceConfigurationService getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    @Override
    public void save() {
        super.save();
        this.deviceProtocolPluggableClassChanged = false;
    }

    @Override
    protected void doDelete() {
        this.registerTypeUsages.clear();
        this.loadProfileTypeUsages.clear();
        this.logBookTypeUsages.clear();
        Iterator<DeviceConfiguration> iterator = this.deviceConfigurations.iterator();
        // do not replace with foreach!! the deviceConfiguration will be removed from the iterator
        while (iterator.hasNext()) {
            ServerDeviceConfiguration deviceConfiguration = (ServerDeviceConfiguration) iterator.next();
            this.notifyDelete(deviceConfiguration);
            deviceConfiguration.notifyDelete();
            deviceConfiguration.prepareDelete();
            iterator.remove();
        }
        this.getDataMapper().remove(this);
    }

    private void notifyDelete(ServerDeviceConfiguration deviceConfiguration) {
        deviceConfiguration.notifyDelete();
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICETYPE;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICETYPE;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICETYPE;
    }

    @Override
    protected void validateDelete() {
        if (this.hasActiveConfigurations()) {
            throw CannotDeleteBecauseStillInUseException.deviceTypeIsStillInUse(this.getThesaurus(), this);
        }
    }

    private boolean hasActiveConfigurations() {
        for (DeviceConfiguration configuration : this.getConfigurations()) {
            if (configuration.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void doSetName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    @Override
    public DeviceUsageType getDeviceUsageType() {
        if (this.deviceUsageType == null) {
            this.deviceUsageType = PersistentDeviceUsageType.fromDb(this.deviceUsageTypeId).toActualType();
        }
        return this.deviceUsageType;
    }

    @Override
    public void setDeviceUsageType(DeviceUsageType deviceUsageType) {
        this.deviceUsageType = deviceUsageType;
        this.deviceUsageTypeId = PersistentDeviceUsageType.fromActual(deviceUsageType).getCode();
    }

    @Override
    public boolean canActAsGateway() {
        if (getDeviceProtocolPluggableClass() == null || getDeviceProtocolPluggableClass().getDeviceProtocol() == null) {
            return false;
        }
        List<DeviceProtocolCapabilities> deviceProtocolCapabilities = getDeviceProtocolPluggableClass().getDeviceProtocol().getDeviceProtocolCapabilities();
        return deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Override
    public boolean isDirectlyAddressable() {
        if (getDeviceProtocolPluggableClass() == null || getDeviceProtocolPluggableClass().getDeviceProtocol() == null) {
            return false;
        }
        List<DeviceProtocolCapabilities> deviceProtocolCapabilities = getDeviceProtocolPluggableClass().getDeviceProtocol().getDeviceProtocolCapabilities();
        return deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        if (this.deviceProtocolPluggableClass == null) {
            this.deviceProtocolPluggableClass = this.findDeviceProtocolPluggableClass(this.deviceProtocolPluggableClassId).get();
        }
        return this.deviceProtocolPluggableClass;
    }

    private Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long deviceProtocolPluggableClassId) {
        return this.protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId);
    }

    @Override
    public void setDeviceProtocolPluggableClass(String deviceProtocolPluggableClassName) {
        this.setDeviceProtocolPluggableClass(this.protocolPluggableService.findDeviceProtocolPluggableClassByName(deviceProtocolPluggableClassName).orElse(null));
    }

    @Override
    public void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        // Test for null because javax.validation only kicks @ save time
        if (deviceProtocolPluggableClass != null) {
            this.deviceProtocolPluggableClassChanged = (this.deviceProtocolPluggableClassId != deviceProtocolPluggableClass.getId());
            this.deviceProtocolPluggableClassId = deviceProtocolPluggableClass.getId();
            this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
        } else {
            this.deviceProtocolPluggableClassChanged = (this.deviceProtocolPluggableClassId != 0);
            this.deviceProtocolPluggableClassId = 0;
            this.deviceProtocolPluggableClass = null;
        }
    }

    boolean deviceProtocolPluggableClassChanged() {
        return deviceProtocolPluggableClassChanged;
    }

    public List<RegisterSpec> getRegisterSpecs() {
        List<RegisterSpec> registerSpecs = new ArrayList<>();
        for (DeviceConfiguration deviceConfiguration : this.deviceConfigurations) {
            registerSpecs.addAll(deviceConfiguration.getRegisterSpecs());
        }
        return registerSpecs;
    }

    @Override
    public List<LogBookType> getLogBookTypes() {
        List<LogBookType> logBookTypes = new ArrayList<>(this.logBookTypeUsages.size());
        for (DeviceTypeLogBookTypeUsage logBookTypeUsage : this.logBookTypeUsages) {
            logBookTypes.add(logBookTypeUsage.getLogBookType());
        }
        return logBookTypes;
    }

    @Override
    public List<RegisterType> getRegisterTypes() {
        List<RegisterType> registerTypes = new ArrayList<>(this.registerTypeUsages.size());
        for (DeviceTypeRegisterTypeUsage registerTypeUsage : this.registerTypeUsages) {
            registerTypes.add(registerTypeUsage.getRegisterType());
        }
        return registerTypes;
    }

    @Override
    public List<LoadProfileType> getLoadProfileTypes() {
        List<LoadProfileType> loadProfileTypes = new ArrayList<>(this.loadProfileTypeUsages.size());
        for (DeviceTypeLoadProfileTypeUsage loadProfileTypeUsage : this.loadProfileTypeUsages) {
            loadProfileTypes.add(loadProfileTypeUsage.getLoadProfileType());
        }
        return loadProfileTypes;
    }

    @Override
    public void addLoadProfileType(LoadProfileType loadProfileType) {
        for (DeviceTypeLoadProfileTypeUsage loadProfileTypeUsage : this.loadProfileTypeUsages) {
            if (loadProfileTypeUsage.sameLoadProfileType(loadProfileType)) {
                throw new LoadProfileTypeAlreadyInDeviceTypeException(this.getThesaurus(), this, loadProfileType);
            }
        }
        this.loadProfileTypeUsages.add(new DeviceTypeLoadProfileTypeUsage(this, loadProfileType));
    }

    @Override
    public void removeLoadProfileType(LoadProfileType loadProfileType) {
        Iterator<DeviceTypeLoadProfileTypeUsage> loadProfileTypeUsageIterator = this.loadProfileTypeUsages.iterator();
        while (loadProfileTypeUsageIterator.hasNext()) {
            DeviceTypeLoadProfileTypeUsage loadProfileTypeUsage = loadProfileTypeUsageIterator.next();
            if (loadProfileTypeUsage.sameLoadProfileType(loadProfileType)) {
                this.validateLoadProfileTypeNotUsedByLoadProfileSpec(loadProfileType);
                loadProfileTypeUsageIterator.remove();
            }
        }
    }

    private void validateLoadProfileTypeNotUsedByLoadProfileSpec(LoadProfileType loadProfileType) {
        List<LoadProfileSpec> loadProfileSpecs = this.getLoadProfileSpecsForLoadProfileType(loadProfileType);
        if (!loadProfileSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUseByLoadProfileSpec(this.thesaurus, loadProfileType, loadProfileSpecs);
        }
    }

    /**
     * Finds all {@link LoadProfileSpec}s from all {@link DeviceConfiguration}s
     * that are using the specified {@link LoadProfileType}.
     *
     * @param loadProfileType The LoadProfileType
     * @return The List of LoadProfileSpec
     */
    private List<LoadProfileSpec> getLoadProfileSpecsForLoadProfileType(LoadProfileType loadProfileType) {
        List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>(0);
        this.collectLoadProfileSpecsForLoadProfileType(loadProfileType, loadProfileSpecs);
        return loadProfileSpecs;
    }

    private void collectLoadProfileSpecsForLoadProfileType(LoadProfileType loadProfileType, List<LoadProfileSpec> loadProfileSpecs) {
        for (DeviceConfiguration deviceConfiguration : this.getConfigurations()) {
            this.collectLoadProfileSpecsForLoadProfileType(loadProfileType, deviceConfiguration, loadProfileSpecs);
        }
    }

    private void collectLoadProfileSpecsForLoadProfileType(LoadProfileType loadProfileType, DeviceConfiguration deviceConfiguration, List<LoadProfileSpec> loadProfileSpecs) {
        for (LoadProfileSpec loadProfileSpec : deviceConfiguration.getLoadProfileSpecs()) {
            if (loadProfileSpec.getLoadProfileType().getId() == loadProfileType.getId()) {
                loadProfileSpecs.add(loadProfileSpec);
            }
        }
    }

    @Override
    public void addLogBookType(LogBookType logBookType) {
        for (DeviceTypeLogBookTypeUsage logBookTypeUsage : this.logBookTypeUsages) {
            if (logBookTypeUsage.sameLogBookType(logBookType)) {
                throw new LogBookTypeAlreadyInDeviceTypeException(this.thesaurus, this, logBookType);
            }
        }
        this.logBookTypeUsages.add(new DeviceTypeLogBookTypeUsage(this, logBookType));
    }

    @Override
    public void addRegisterType(RegisterType registerType) {
        for (DeviceTypeRegisterTypeUsage registerTypeUsage : this.registerTypeUsages) {
            if (registerTypeUsage.sameRegisterType(registerType)) {
                throw new RegisterTypeAlreadyInDeviceTypeException(this.getThesaurus(), this, registerType);
            }
        }
        this.registerTypeUsages.add(new DeviceTypeRegisterTypeUsage(this, registerType));
    }

    @Override
    public void removeRegisterType(RegisterType registerType) {
        Iterator<DeviceTypeRegisterTypeUsage> iterator = this.registerTypeUsages.iterator();
        while (iterator.hasNext()) {
            DeviceTypeRegisterTypeUsage registerTypeUsage = iterator.next();
            if (registerTypeUsage.sameRegisterType(registerType)) {
                this.validateRegisterTypeNotUsedByRegisterSpec(registerType);
                this.validateRegisterTypeNotUsedByChannelSpec(registerType);
                iterator.remove();
            }
        }
    }

    private void validateRegisterTypeNotUsedByChannelSpec(MeasurementType measurementType) {
        List<ChannelSpec> channelSpecs = this.getChannelSpecsForChannelType(measurementType);
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.channelTypeIsStillInUseByChannelSpecs(this.thesaurus, measurementType, channelSpecs);
        }
    }

    private List<ChannelSpec> getChannelSpecsForChannelType(MeasurementType measurementType) {
        List<ChannelSpec> channelSpecs = new ArrayList<>();
        this.collectChannelSpecsForChannelType(measurementType, channelSpecs);
        return channelSpecs;
    }

    private void collectChannelSpecsForChannelType(MeasurementType measurementType, List<ChannelSpec> channelSpecs) {
        for (DeviceConfiguration deviceConfiguration : this.getConfigurations()) {
            this.collectChannelSpecsForChannelType(measurementType, deviceConfiguration, channelSpecs);
        }
    }

    private void collectChannelSpecsForChannelType(MeasurementType measurementType, DeviceConfiguration deviceConfiguration, List<ChannelSpec> channelSpecs) {
        for (ChannelSpec channelSpec : deviceConfiguration.getChannelSpecs()) {
            if (channelSpec.getChannelType().getId() == measurementType.getId()) {
                channelSpecs.add(channelSpec);
            }
        }
    }

    private void validateRegisterTypeNotUsedByRegisterSpec(MeasurementType measurementType) {
        List<RegisterSpec> registerSpecs = this.getRegisterSpecsForRegisterType(measurementType);
        if (!registerSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerTypeIsStillInUseByRegisterSpecs(this.thesaurus, measurementType, registerSpecs);
        }
    }

    private List<RegisterSpec> getRegisterSpecsForRegisterType(MeasurementType measurementType) {
        List<RegisterSpec> registerSpecs = new ArrayList<>();
        this.collectRegisterSpecsForRegisterType(measurementType, registerSpecs);
        return registerSpecs;
    }

    private void collectRegisterSpecsForRegisterType(MeasurementType measurementType, List<RegisterSpec> registerSpecs) {
        for (DeviceConfiguration deviceConfiguration : this.getConfigurations()) {
            this.collectRegisterSpecsForRegisterType(measurementType, deviceConfiguration, registerSpecs);
        }
    }

    private void collectRegisterSpecsForRegisterType(MeasurementType measurementType, DeviceConfiguration deviceConfiguration, List<RegisterSpec> registerSpecs) {
        for (RegisterSpec registerSpec : deviceConfiguration.getRegisterSpecs()) {
            if (registerSpec.getRegisterType().getId() == measurementType.getId()) {
                registerSpecs.add(registerSpec);
            }
        }
    }

    @Override
    public void removeLogBookType(LogBookType logBookType) {
        Iterator<DeviceTypeLogBookTypeUsage> logBookTypeUsageIterator = this.logBookTypeUsages.iterator();
        while (logBookTypeUsageIterator.hasNext()) {
            DeviceTypeLogBookTypeUsage logBookTypeUsage = logBookTypeUsageIterator.next();
            if (logBookTypeUsage.sameLogBookType(logBookType)) {
                this.validateLogBookTypeNotUsedByLogBookSpec(logBookType);
                logBookTypeUsageIterator.remove();
            }
        }
    }

    private void validateLogBookTypeNotUsedByLogBookSpec(LogBookType logBookType) {
        List<LogBookSpec> logBookSpecs = this.getLogBookSpecsForLogBookType(logBookType);
        if (!logBookSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.logBookTypeIsStillInUseByLogBookSpec(this.thesaurus, logBookType, logBookSpecs);
        }
    }

    /**
     * Finds all {@link LogBookSpec}s from all {@link DeviceConfiguration}s
     * that are using the specified {@link LogBookType}.
     *
     * @param logBookType The LogBookType
     * @return The List of LogBookSpec
     */
    private List<LogBookSpec> getLogBookSpecsForLogBookType(LogBookType logBookType) {
        List<LogBookSpec> logBookSpecs = new ArrayList<>(0);
        this.collectLogBookSpecsForLogBookType(logBookType, logBookSpecs);
        return logBookSpecs;
    }

    private void collectLogBookSpecsForLogBookType(LogBookType logBookType, List<LogBookSpec> logBookSpecs) {
        for (DeviceConfiguration deviceConfiguration : this.getConfigurations()) {
            this.collectLogBookSpecsForLogBookType(logBookType, deviceConfiguration, logBookSpecs);
        }
    }

    private void collectLogBookSpecsForLogBookType(LogBookType logBookType, DeviceConfiguration deviceConfiguration, List<LogBookSpec> logBookSpecs) {
        for (LogBookSpec logBookSpec : deviceConfiguration.getLogBookSpecs()) {
            if (logBookSpec.getLogBookType().getId() == logBookType.getId()) {
                logBookSpecs.add(logBookSpec);
            }
        }
    }

    public boolean supportsMessaging() {
        return this.getDeviceProtocolPluggableClass() != null;
    }

    public boolean isChannelJournalUsed() {
        return this.useChannelJournal;
    }

    public boolean isLogicalSlave() {
        List<DeviceProtocolCapabilities> deviceProtocolCapabilities = this.getLocalDeviceProtocol().getDeviceProtocolCapabilities();
        return deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_SLAVE) && deviceProtocolCapabilities.size() == 1;
    }

    private DeviceProtocol getLocalDeviceProtocol() {
        if (localDeviceProtocol == null) {
            localDeviceProtocol = this.getDeviceProtocolPluggableClass().getDeviceProtocol();
        }
        return localDeviceProtocol;
    }

    @Override
    public List<DeviceConfiguration> getConfigurations() {
        return ImmutableList.copyOf(this.deviceConfigurations);
    }

    private void addConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfigurations.add(deviceConfiguration);
    }

    @Override
    public void removeConfiguration(DeviceConfiguration deviceConfigurationToDelete) {
        Iterator<DeviceConfiguration> iterator = this.deviceConfigurations.iterator();
        while (iterator.hasNext()) {
            ServerDeviceConfiguration configuration = (ServerDeviceConfiguration) iterator.next();
            if (configuration.getId() == deviceConfigurationToDelete.getId()) {
                configuration.notifyDelete();
                configuration.prepareDelete();
                iterator.remove();
            }
        }
    }

    @Override
    public DeviceConfigurationBuilder newConfiguration(String name) {
        return new ConfigurationBuilder(this.dataModel.getInstance(DeviceConfigurationImpl.class).initialize(this, name));
    }

    private enum BuildingMode {
        UNDERCONSTRUCTION {
            @Override
            protected void verify() {
                // All calls are fine as long as we are under construction
            }
        },
        COMPLETE {
            @Override
            protected void verify() {
                throw new IllegalStateException("The device configuration building process is already complete");
            }
        };

        protected abstract void verify();
    }

    private interface NestedBuilder {

        public void add();
    }

    private class ChannelSpecBuilder implements NestedBuilder {

        private final ChannelSpec.ChannelSpecBuilder builder;

        private ChannelSpecBuilder(ChannelSpec.ChannelSpecBuilder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private class NumericalRegisterSpecBuilder implements NestedBuilder {

        private final NumericalRegisterSpec.Builder builder;

        private NumericalRegisterSpecBuilder(NumericalRegisterSpec.Builder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private class TextualRegisterSpecBuilder implements NestedBuilder {

        private final TextualRegisterSpec.Builder builder;

        private TextualRegisterSpecBuilder(TextualRegisterSpec.Builder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private class LoadProfileSpecBuilder implements NestedBuilder {

        private final LoadProfileSpec.LoadProfileSpecBuilder builder;

        private LoadProfileSpecBuilder(LoadProfileSpec.LoadProfileSpecBuilder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private class LogBookSpecBuilder implements NestedBuilder {

        private final LogBookSpec.LogBookSpecBuilder builder;

        private LogBookSpecBuilder(LogBookSpec.LogBookSpecBuilder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private class ConfigurationBuilder implements DeviceConfigurationBuilder {

        private BuildingMode mode;
        private final DeviceConfiguration underConstruction;
        private final List<NestedBuilder> nestedBuilders = new ArrayList<>();

        private ConfigurationBuilder(DeviceConfiguration underConstruction) {
            super();
            this.mode = BuildingMode.UNDERCONSTRUCTION;
            this.underConstruction = underConstruction;
        }

        @Override
        public DeviceConfigurationBuilder description(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public DeviceConfigurationBuilder isDirectlyAddressable(boolean isDirectlyAddressable) {
            underConstruction.setDirectlyAddressable(isDirectlyAddressable);
            return this;
        }

        @Override
        public DeviceConfigurationBuilder gatewayType(GatewayType gatewayType) {
            if (gatewayType != null && !GatewayType.NONE.equals(gatewayType)){
                canActAsGateway(true);
            }
            underConstruction.setGatewayType(gatewayType);
            return this;
        }

        @Override
        public DeviceConfigurationBuilder canActAsGateway(boolean canActAsGateway) {
            underConstruction.setCanActAsGateway(canActAsGateway);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(ChannelType channelType, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
            ChannelSpec.ChannelSpecBuilder builder = this.underConstruction.createChannelSpec(channelType, phenomenon, loadProfileSpec);
            this.nestedBuilders.add(new ChannelSpecBuilder(builder));
            return builder;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(ChannelType channelType, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
            ChannelSpec.ChannelSpecBuilder builder = this.underConstruction.createChannelSpec(channelType, phenomenon, loadProfileSpecBuilder);
            this.nestedBuilders.add(new ChannelSpecBuilder(builder));
            return builder;
        }

        @Override
        public NumericalRegisterSpec.Builder newNumericalRegisterSpec(RegisterType registerType) {
            NumericalRegisterSpec.Builder builder = this.underConstruction.createNumericalRegisterSpec(registerType);
            this.nestedBuilders.add(new NumericalRegisterSpecBuilder(builder));
            return builder;
        }

        @Override
        public TextualRegisterSpec.Builder newTextualRegisterSpec(RegisterType registerType) {
            TextualRegisterSpec.Builder builder = this.underConstruction.createTextualRegisterSpec(registerType);
            this.nestedBuilders.add(new TextualRegisterSpecBuilder(builder));
            return builder;
        }

        @Override
        public LoadProfileSpec.LoadProfileSpecBuilder newLoadProfileSpec(LoadProfileType loadProfileType) {
            LoadProfileSpec.LoadProfileSpecBuilder builder = this.underConstruction.createLoadProfileSpec(loadProfileType);
            this.nestedBuilders.add(new LoadProfileSpecBuilder(builder));
            return builder;
        }

        @Override
        public LogBookSpec.LogBookSpecBuilder newLogBookSpec(LogBookType logBookType) {
            LogBookSpec.LogBookSpecBuilder builder = this.underConstruction.createLogBookSpec(logBookType);
            this.nestedBuilders.add(new LogBookSpecBuilder(builder));
            return builder;
        }

        @Override
        public DeviceConfiguration add() {
            this.mode.verify();
            this.doNestedBuilders();
            Save.CREATE.validate(dataModel, this.underConstruction);
            addConfiguration(this.underConstruction);
            this.mode = BuildingMode.COMPLETE;
            this.underConstruction.getDeviceType().getDeviceProtocolPluggableClass()
                    .getDeviceProtocol().getSupportedMessages().stream().forEach(
                    deviceMessageId -> {
                        DeviceMessageEnablementBuilder deviceMessageEnablement = underConstruction.createDeviceMessageEnablement(deviceMessageId);
                        deviceMessageEnablement.addUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
                        deviceMessageEnablement.addUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2);
                        deviceMessageEnablement.addUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);
                        deviceMessageEnablement.build();
                    });
            return this.underConstruction;
        }

        private void doNestedBuilders() {
            for (NestedBuilder nestedBuilder : this.nestedBuilders) {
                nestedBuilder.add();
            }
        }
    }

}