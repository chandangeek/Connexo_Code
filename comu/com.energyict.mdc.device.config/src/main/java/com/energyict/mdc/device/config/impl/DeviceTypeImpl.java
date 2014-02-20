package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceCommunicationFunction;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceUsageType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotChangeDeviceProtocolWithActiveConfigurationsException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.DeviceProtocolIsRequiredException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.LogBookTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingAlreadyInDeviceTypeException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DeviceTypeImpl extends PersistentNamedObject<DeviceType> implements DeviceType {

    private int channelCount;
    private String description;
    private boolean useChannelJournal;
    private int deviceUsageTypeId;
    private DeviceUsageType deviceUsageType;
    private int communicationFunctionMask;
    private Set<DeviceCommunicationFunction> deviceCommunicationFunctions;
    private List<DeviceConfiguration> deviceConfigurations = new ArrayList<>();
    private List<DeviceTypeLogBookTypeUsage> logBookTypeUsages = new ArrayList<>();
    private List<DeviceTypeLoadProfileTypeUsage> loadProfileTypeUsages = new ArrayList<>();
    private List<DeviceTypeRegisterMappingUsage> registerMappingUsages = new ArrayList<>();
    private long prototypeId;

    private long deviceProtocolPluggableClassId;
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
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

    @Override
    protected NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus) {
        return NameIsRequiredException.deviceTypeNameIsRequired(thesaurus);
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.deviceTypeExists(thesaurus, name);
    }

    @Override
    protected void postNew() {
        this.getDataMapper().persist(this);
    }

    @Override
    protected void post() {
        this.getDataMapper().update(this);
    }

    @Override
    protected void doDelete() {
        this.registerMappingUsages.clear();
        this.loadProfileTypeUsages.clear();
        this.logBookTypeUsages.clear();
        for (DeviceConfiguration deviceConfiguration : this.deviceConfigurations) {
            this.notifyDelete((ServerDeviceConfiguration) deviceConfiguration);
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
            if (configuration.getActive()) {
                return true;
            }
        }
        return false;
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
    public Set<DeviceCommunicationFunction> getCommunicationFunctions() {
        if (this.deviceCommunicationFunctions == null) {
            this.deviceCommunicationFunctions = this.createSetFromMasks(this.communicationFunctionMask);
        }
        return EnumSet.copyOf(this.deviceCommunicationFunctions);
    }

    private Set<DeviceCommunicationFunction> createSetFromMasks(int communicationFunctionMask) {
        return new DeviceCommunicationFunctionSetPersister().fromDb(communicationFunctionMask);
    }

    public boolean hasCommunicationFunction(DeviceCommunicationFunction function) {
        return this.getCommunicationFunctions().contains(function);
    }

    @Override
    public void addCommunicationFunction(DeviceCommunicationFunction function) {
        this.getCommunicationFunctions();   // Load the current set
        this.deviceCommunicationFunctions.add(function);
        this.communicationFunctionMask = new DeviceCommunicationFunctionSetPersister().toDb(this.deviceCommunicationFunctions);
    }

    @Override
    public void removeCommunicationFunction(DeviceCommunicationFunction function) {
        this.getCommunicationFunctions();   // Load the current set
        this.deviceCommunicationFunctions.remove(function);
        this.communicationFunctionMask = new DeviceCommunicationFunctionSetPersister().toDb(this.deviceCommunicationFunctions);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        if (this.deviceProtocolPluggableClass == null) {
            this.deviceProtocolPluggableClass = this.findDeviceProtocolPluggableClass(this.deviceProtocolPluggableClassId);
        }
        return this.deviceProtocolPluggableClass;
    }

    private DeviceProtocolPluggableClass findDeviceProtocolPluggableClass(long deviceProtocolPluggableClassId) {
        return this.protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId);
    }

    @Override
    public void setDeviceProtocolPluggableClass(String deviceProtocolPluggableClassName) {
        this.setDeviceProtocolPluggableClass(this.protocolPluggableService.findDeviceProtocolPluggableClassByName(deviceProtocolPluggableClassName));
    }

    @Override
    public void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        if (deviceProtocolPluggableClass == null) {
            throw new DeviceProtocolIsRequiredException(this.getThesaurus());
        }
        if (this.hasActiveConfigurations()) {
            throw new CannotChangeDeviceProtocolWithActiveConfigurationsException(this.thesaurus, this);
        }
        this.deviceProtocolPluggableClassId = deviceProtocolPluggableClass.getId();
        this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
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
    public List<RegisterMapping> getRegisterMappings() {
        List<RegisterMapping> registerMappings = new ArrayList<>(this.registerMappingUsages.size());
        for (DeviceTypeRegisterMappingUsage registerMappingUsage : this.registerMappingUsages) {
            registerMappings.add(registerMappingUsage.getRegisterMapping());
        }
        return registerMappings;
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
    public void addRegisterMapping(RegisterMapping registerMapping) {
        for (DeviceTypeRegisterMappingUsage registerMappingUsage : this.registerMappingUsages) {
            if (registerMappingUsage.sameRegisterMapping(registerMapping)) {
                throw new RegisterMappingAlreadyInDeviceTypeException(this.getThesaurus(), this, registerMapping);
            }
        }
        this.registerMappingUsages.add(new DeviceTypeRegisterMappingUsage(this, registerMapping));
    }

    @Override
    public void removeRegisterMapping(RegisterMapping registerMapping) {
        Iterator<DeviceTypeRegisterMappingUsage> iterator = this.registerMappingUsages.iterator();
        while (iterator.hasNext()) {
            DeviceTypeRegisterMappingUsage registerMappingUsage = iterator.next();
            if (registerMappingUsage.sameRegisterMapping(registerMapping)) {
                this.validateRegisterMappingNotUsedByRegisterSpec(registerMapping);
                this.validateRegisterMappingNotUsedByChannelSpec(registerMapping);
                iterator.remove();
            }
        }
    }

    private void validateRegisterMappingNotUsedByChannelSpec(RegisterMapping registerMapping) {
        List<ChannelSpec> channelSpecs = this.getChannelSpecsForRegisterMapping(registerMapping);
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByChannelSpecs(this.thesaurus, registerMapping, channelSpecs);
        }
    }

    private List<ChannelSpec> getChannelSpecsForRegisterMapping(RegisterMapping registerMapping) {
        List<ChannelSpec> channelSpecs = new ArrayList<>();
        this.collectChannelSpecsForRegisterMapping(registerMapping, channelSpecs);
        return channelSpecs;
    }

    private void collectChannelSpecsForRegisterMapping(RegisterMapping registerMapping, List<ChannelSpec> channelSpecs) {
        for (DeviceConfiguration deviceConfiguration : this.getConfigurations()) {
            this.collectChannelSpecsForRegisterMapping(registerMapping, deviceConfiguration, channelSpecs);
        }
    }

    private void collectChannelSpecsForRegisterMapping(RegisterMapping registerMapping, DeviceConfiguration deviceConfiguration, List<ChannelSpec> channelSpecs) {
        for (ChannelSpec channelSpec : deviceConfiguration.getChannelSpecs()) {
            if(channelSpec.getRegisterMapping().getId() == registerMapping.getId()){
                channelSpecs.add(channelSpec);
            }
        }
    }

    private void validateRegisterMappingNotUsedByRegisterSpec(RegisterMapping registerMapping) {
        List<RegisterSpec> registerSpecs = this.getRegisterSpecsForRegisterMapping(registerMapping);
        if (!registerSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByRegisterSpec(this.thesaurus, registerMapping, registerSpecs);
        }
    }

    private List<RegisterSpec> getRegisterSpecsForRegisterMapping(RegisterMapping registerMapping) {
        List<RegisterSpec> registerSpecs = new ArrayList<>();
        this.collectRegisterSpecsForRegisterMapping(registerMapping, registerSpecs);
        return registerSpecs;
    }

    private void collectRegisterSpecsForRegisterMapping(RegisterMapping registerMapping, List<RegisterSpec> registerSpecs) {
        for (DeviceConfiguration deviceConfiguration : this.getConfigurations()) {
            this.collectRegisterSpecsForRegisterMapping(registerMapping, deviceConfiguration, registerSpecs);
        }
    }

    private void collectRegisterSpecsForRegisterMapping(RegisterMapping registerMapping, DeviceConfiguration deviceConfiguration, List<RegisterSpec> registerSpecs) {
        for (RegisterSpec registerSpec : deviceConfiguration.getRegisterSpecs()) {
            if (registerSpec.getRegisterMapping().getId() == registerMapping.getId()) {
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

    public int getCommunicationFunctionMask() {
        return communicationFunctionMask;
    }

    @Override
    public List<DeviceConfiguration> getConfigurations() {
        return ImmutableList.copyOf(this.deviceConfigurations);
    }

    private void addConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfigurations.add(deviceConfiguration);
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

    private class RegisterSpecBuilder implements NestedBuilder {

        private final RegisterSpec.RegisterSpecBuilder builder;

        private RegisterSpecBuilder(RegisterSpec.RegisterSpecBuilder builder) {
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
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
            ChannelSpec.ChannelSpecBuilder builder = this.underConstruction.createChannelSpec(registerMapping, phenomenon, loadProfileSpec);
            this.nestedBuilders.add(new ChannelSpecBuilder(builder));
            return builder;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
            ChannelSpec.ChannelSpecBuilder builder = this.underConstruction.createChannelSpec(registerMapping, phenomenon, loadProfileSpecBuilder);
            this.nestedBuilders.add(new ChannelSpecBuilder(builder));
            return builder;
        }

        @Override
        public RegisterSpec.RegisterSpecBuilder newRegisterSpec(RegisterMapping registerMapping) {
            RegisterSpec.RegisterSpecBuilder builder = this.underConstruction.createRegisterSpec(registerMapping);
            this.nestedBuilders.add(new RegisterSpecBuilder(builder));
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
            addConfiguration(this.underConstruction);
            this.mode = BuildingMode.COMPLETE;
            return this.underConstruction;
        }

        private void doNestedBuilders() {
            for (NestedBuilder nestedBuilder : this.nestedBuilders) {
                nestedBuilder.add();
            }
        }
    }

}