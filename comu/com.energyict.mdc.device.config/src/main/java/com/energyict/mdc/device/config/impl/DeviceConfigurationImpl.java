package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceCommunicationConfigurationFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.HasId;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigurationIsActiveException;
import com.energyict.mdc.device.config.exceptions.DeviceTypeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.DuplicateLoadProfileTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateLogBookTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdc.protocol.api.device.DeviceFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *     //TODO the creation of the CommunicationConfiguration is currently skipped ...
 *
 * User: gde
 * Date: 5/11/12
 */
public class DeviceConfigurationImpl extends PersistentNamedObject<DeviceConfiguration> implements DeviceConfiguration, ServerDeviceConfiguration {

    private String description;

    private boolean active;
    private int prototypeId;
    private Device prototype;

    private final Reference<DeviceType> deviceType = ValueReference.absent();
    @Valid
    private List<RegisterSpec> registerSpecs = new ArrayList<>();
    @Valid
    private List<ChannelSpec> channelSpecs = new ArrayList<>();
    @Valid
    private List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>();
    @Valid
    private List<LogBookSpec> logBookSpecs = new ArrayList<>();
    private DeviceCommunicationConfiguration communicationConfiguration;
    private Date modificationDate;
    private Clock clock;
    private final Provider<LoadProfileSpecImpl> loadProfileSpecProvider;
    private final Provider<RegisterSpecImpl> registerSpecProvider;
    private final Provider<LogBookSpecImpl> logBookSpecProvider;
    private final Provider<ChannelSpecImpl> channelSpecProvider;

    @Inject
    protected DeviceConfigurationImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock,
                                      Provider<LoadProfileSpecImpl> loadProfileSpecProvider,
                                      Provider<RegisterSpecImpl> registerSpecProvider,
                                      Provider<LogBookSpecImpl> logBookSpecProvider,
                                      Provider<ChannelSpecImpl> channelSpecProvider) {
        super(DeviceConfiguration.class, dataModel, eventService, thesaurus);
        this.clock = clock;

        this.loadProfileSpecProvider = loadProfileSpecProvider;
        this.registerSpecProvider = registerSpecProvider;
        this.logBookSpecProvider = logBookSpecProvider;
        this.channelSpecProvider = channelSpecProvider;
    }

    DeviceConfigurationImpl initialize(DeviceType deviceType, String name){
        this.deviceType.set(deviceType);
        setName(name);
        return this;
    }

    @Override
    public DeviceCommunicationConfiguration getCommunicationConfiguration() {
        if (this.communicationConfiguration == null) {
            List<DeviceCommunicationConfigurationFactory> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceCommunicationConfigurationFactory.class);
            if (!modulesImplementing.isEmpty()) {
                this.communicationConfiguration = modulesImplementing.get(0).findFor(this);
            }
        }
        return this.communicationConfiguration;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getPrototypeId() {
        return getPrototypeDevice() == null ? 0 : getPrototypeDevice().getId();
    }

    @Override
    public Device getPrototypeDevice() {
        if(this.prototype == null && prototypeId > 0){
            List<DeviceFactory> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceFactory.class);
            if(!modulesImplementing.isEmpty()){
                this.prototype = modulesImplementing.get(0).findById(prototypeId);
            }
        }
        return this.prototype;
    }

    @Override
    protected void validateUniqueName(String name) {
        for (DeviceConfiguration deviceConfiguration : this.deviceType.get().getConfigurations()) {
            if(!isSameIdObject(deviceConfiguration, this) && deviceConfiguration.getName().equals(name)){
                throw this.duplicateNameException(this.getThesaurus(), name);
            }
        }
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.deviceConfigurationExists(thesaurus, name);
    }

    @Override
    public void notifyDelete() {
        validateDelete();
    }

    @Override
    public void validateUpdateLoadProfileType(LoadProfileType loadProfileType) {
        this.validateAllLoadProfileSpecsHaveUniqueObisCodes(loadProfileType);
    }

    private void validateAllLoadProfileSpecsHaveUniqueObisCodes(LoadProfileType loadProfileType) {
        Set<String> loadProfileObisCodes = new HashSet<>();
        for (LoadProfileSpec each : this.getLoadProfileSpecs()) {
            String obisCodeValue = each.getObisCode().toString();
            if (!loadProfileObisCodes.contains(obisCodeValue)) {
                loadProfileObisCodes.add(obisCodeValue);
            }
            else {
                throw new DuplicateLoadProfileTypeException(this.thesaurus, this, loadProfileType, each);
            }
        }
    }

    @Override
    public void validateUpdateLogBookType(LogBookType logBookType) {
        this.validateAllLogBookTypesHaveUniqueObisCodes();
    }

    private void validateAllLogBookTypesHaveUniqueObisCodes() {
        Map<String, String> obisCodeAndNameMap = new HashMap<>();
        for (LogBookSpec each : this.getLogBookSpecs()) {
            LogBookType eachLogBookType = each.getLogBookType();
            String obisCodeValue = this.findNextAvailableObisCode(eachLogBookType.getObisCode().getValue(), obisCodeAndNameMap.keySet());
            String logBookSpecName = eachLogBookType.getName();
            if (!obisCodeAndNameMap.containsKey(obisCodeValue)) {
                obisCodeAndNameMap.put(obisCodeValue, logBookSpecName);
            }
            else {
                throw DuplicateObisCodeException.forLogBookSpec(this.thesaurus, this, each.getDeviceObisCode(), each);
            }
        }
    }

    @Override
    public void validateUpdateRegisterMapping(RegisterMapping registerMapping) {
        this.validateAllChannelSpecsHaveUniqueObisCodes();
        this.validateAllRegisterSpecsHaveUniqueObisCodes();
    }

    private void validateAllChannelSpecsHaveUniqueObisCodes() {
        Map<Long, Set<String>> loadProfileTypeObisCodes = new HashMap<>();
        for (ChannelSpec each : this.getChannelSpecs()) {
            ObisCode obisCode = each.getRegisterMapping().getObisCode();
            String obisCodeValue = obisCode.getValue();
            long loadProfileTypeId = each.getLoadProfileSpec().getLoadProfileType().getId();
            Set<String> obisCodesForLoadProfileType = loadProfileTypeObisCodes.get(loadProfileTypeId);
            if (obisCodesForLoadProfileType == null) {
                obisCodesForLoadProfileType = new HashSet<>();
                loadProfileTypeObisCodes.put(loadProfileTypeId, obisCodesForLoadProfileType);
            }
            if (!obisCodesForLoadProfileType.contains(obisCodeValue)) {
                obisCodesForLoadProfileType.add(obisCodeValue);
            }
            else {
                if (!obisCode.anyChannel()) {
                    throw DuplicateObisCodeException.forChannelSpecInLoadProfileSpec(this.thesaurus, this, each.getDeviceObisCode(), each, each.getLoadProfileSpec());
                }
            }
        }
    }

    private void validateAllRegisterSpecsHaveUniqueObisCodes() {
        Set<String> obisCodeSet = new HashSet<>();
        for (RegisterSpec registerSpec : this.getRegisterSpecs()) {
            String obisCodeValue = this.findNextAvailableObisCode(registerSpec.getDeviceObisCode().toString(), obisCodeSet);
            if (!obisCodeSet.contains(obisCodeValue)) {
                obisCodeSet.add(obisCodeValue);
            }
            else {
                throw DuplicateObisCodeException.forRegisterSpec(this.thesaurus, this, registerSpec.getDeviceObisCode(), registerSpec);
            }
        }
    }

    /**
     * Looks for the next available Obiscode with different B-field.
     *
     * @param obisCodeValue
     * @param obisCodeKeys
     * @return
     */
    private String findNextAvailableObisCode(String obisCodeValue, Collection<String> obisCodeKeys) {
        String availableObisCode = obisCodeValue;
        while (obisCodeKeys.contains(availableObisCode)) {
            ObisCode obisCode = ObisCode.fromString(availableObisCode).nextB();
            availableObisCode = obisCode.toString();
        }
        return availableObisCode;
    }

    @Override
    public DeviceType getDeviceType() {
        return this.deviceType.get();
    }

    @Override
    public List<RegisterSpec> getRegisterSpecs() {
        return Collections.unmodifiableList(this.registerSpecs);
    }

    @Override
    public RegisterSpec.RegisterSpecBuilder createRegisterSpec(RegisterMapping registerMapping) {
        return new RegisterSpecBuilderForConfig(registerSpecProvider, this, registerMapping);
    }

    class RegisterSpecBuilderForConfig extends RegisterSpecImpl.RegisterSpecBuilder {

        RegisterSpecBuilderForConfig(Provider<RegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping) {
            super(registerSpecProvider, deviceConfiguration, registerMapping);
        }

        @Override
        public RegisterSpec add() {
            RegisterSpec registerSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewRegisterSpec(getThesaurus()));
            validateUniqueRegisterSpecObisCode(registerSpec);
            DeviceConfigurationImpl.this.registerSpecs.add(registerSpec);
            return registerSpec;
        }
    }

    @Override
    public RegisterSpec.RegisterSpecUpdater getRegisterSpecUpdaterFor(RegisterSpec registerSpec) {
        return new RegisterSpecUpdaterForConfig(registerSpec);
    }

    class RegisterSpecUpdaterForConfig extends RegisterSpecImpl.RegisterSpecUpdater {

        RegisterSpecUpdaterForConfig(RegisterSpec registerSpec) {
            super(registerSpec);
        }

        @Override
        public void update() {
            validateUniqueRegisterSpecObisCode(registerSpec);
            super.update();
        }
    }

    private void validateUniqueRegisterSpecObisCode(RegisterSpec registerSpec) {
        for (RegisterSpec spec : registerSpecs) {
            if (!isSameIdObject(registerSpec, spec) && spec.getDeviceObisCode().equals(registerSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forRegisterSpec(thesaurus, this, registerSpec.getDeviceObisCode(), registerSpec);
            }
        }
    }

    public void deleteRegisterSpec(RegisterSpec registerSpec) {
        // TODO Complete!!!

//        if (getActive() && !shadow.getRegisterSpecShadows().getDeletedShadows().isEmpty()) {
//            throw new BusinessException("deleteRegisterSpecsFromActiveDeviceConfigIsNotAllowed",
//                    "It's not allowed to delete register specifications of an active device configuration");
//        }
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.canNotDeleteRegisterSpec(this.thesaurus, this, registerSpec);
        }
        registerSpec.validateDelete();
        this.registerSpecs.remove(registerSpec);
        this.eventService.postEvent(EventType.DEVICETYPE_DELETED.topic(),registerSpec);
    }

    @Override
    public List<ChannelSpec> getChannelSpecs() {
        return Collections.unmodifiableList(this.channelSpecs);
    }

    @Override
    public ChannelSpec.ChannelSpecBuilder createChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
        return new ChannelSpecBuilderForConfig(channelSpecProvider, this, registerMapping, phenomenon, loadProfileSpec);
    }

    @Override
    public ChannelSpec.ChannelSpecBuilder createChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
        return new ChannelSpecBuilderForConfig(channelSpecProvider, this, registerMapping, phenomenon, loadProfileSpecBuilder);
    }

    class ChannelSpecBuilderForConfig extends ChannelSpecImpl.ChannelSpecBuilder {

        ChannelSpecBuilderForConfig(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
            super(channelSpecProvider, deviceConfiguration, registerMapping, phenomenon, loadProfileSpec);
        }

        ChannelSpecBuilderForConfig(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
            super(channelSpecProvider, deviceConfiguration, registerMapping, phenomenon, loadProfileSpecBuilder);
        }

        @Override
        public ChannelSpec add() {
            ChannelSpec channelSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewChannelSpec(getThesaurus()));
            validateUniqueChannelSpecPerLoadProfileSpec(channelSpec);
            DeviceConfigurationImpl.this.channelSpecs.add(channelSpec);
            return channelSpec;
        }
    }

    @Override
    public ChannelSpec.ChannelSpecUpdater getChannelSpecUpdaterFor(ChannelSpec channelSpec) {
        return new ChannelSpecUpdaterForConfig((ChannelSpecImpl) channelSpec);
    }

    class ChannelSpecUpdaterForConfig extends ChannelSpecImpl.ChannelSpecUpdater {

        protected ChannelSpecUpdaterForConfig(ChannelSpecImpl channelSpec) {
            super(channelSpec);
        }

        @Override
        public void update() {
            validateUniqueChannelSpecPerLoadProfileSpec(channelSpec);
            super.update();
        }
    }

    private void validateUniqueChannelSpecPerLoadProfileSpec(ChannelSpec channelSpec) {
        for (ChannelSpec spec : channelSpecs) {
            if(!isSameIdObject(spec, channelSpec)){
                if (channelSpec.getLoadProfileSpec() == null) {
                    if (spec.getLoadProfileSpec() == null && channelSpec.getDeviceObisCode().equals(spec.getDeviceObisCode())) {
                        throw DuplicateObisCodeException.forChannelSpecConfigWithoutLoadProfileSpec(thesaurus, this, channelSpec.getDeviceObisCode(), channelSpec);
                    }
                } else if (channelSpec.getLoadProfileSpec().getId() == spec.getLoadProfileSpec().getId()) {
                    if (channelSpec.getDeviceObisCode().equals(spec.getDeviceObisCode())) {
                        throw DuplicateObisCodeException.forChannelSpecInLoadProfileSpec(thesaurus, this, channelSpec.getDeviceObisCode(), channelSpec, channelSpec.getLoadProfileSpec());
                    }
                }
            }
        }
    }

    public void deleteChannelSpec(ChannelSpec channelSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forChannelSpec(this.thesaurus, channelSpec, this);
        }
        channelSpec.validateDelete();
        this.channelSpecs.remove(channelSpec);
        this.eventService.postEvent(EventType.DEVICETYPE_DELETED.topic(),channelSpec);
    }

    @Override
    public List<LoadProfileSpec> getLoadProfileSpecs() {
        return Collections.unmodifiableList(this.loadProfileSpecs);
    }

    @Override
    public LoadProfileSpec.LoadProfileSpecBuilder createLoadProfileSpec(LoadProfileType loadProfileType) {
        return new LoadProfileSpecBuilderForConfig(loadProfileSpecProvider, this, loadProfileType);
    }

    class LoadProfileSpecBuilderForConfig extends LoadProfileSpecImpl.LoadProfileSpecBuilder {

        LoadProfileSpecBuilderForConfig(Provider<LoadProfileSpecImpl> loadProfileSpecProvider, DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType) {
            super(loadProfileSpecProvider, deviceConfiguration, loadProfileType);
        }

        public LoadProfileSpec add() {
            LoadProfileSpec loadProfileSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewLoadProfileSpec(getThesaurus()));
            validateUniqueLoadProfileType(loadProfileSpec);
            validateUniqueLoadProfileObisCode(loadProfileSpec);
            DeviceConfigurationImpl.this.loadProfileSpecs.add(loadProfileSpec);
            return loadProfileSpec;
        }
    }

    @Override
    public LoadProfileSpec.LoadProfileSpecUpdater getLoadProfileSpecUpdaterFor(LoadProfileSpec loadProfileSpec) {
        return new LoadProfileSpecUpdater((LoadProfileSpecImpl) loadProfileSpec);
    }

    private class LoadProfileSpecUpdater extends LoadProfileSpecImpl.LoadProfileSpecUpdater {

        protected LoadProfileSpecUpdater(LoadProfileSpecImpl loadProfileSpec) {
            super(loadProfileSpec);
        }

        @Override
        public void update() {
            validateUniqueLoadProfileObisCode(loadProfileSpec);
            super.update();
        }
    }

    private void validateUniqueLoadProfileObisCode(LoadProfileSpec loadProfileSpec) {
        for (LoadProfileSpec profileSpec : loadProfileSpecs) {
            if (!isSameIdObject(loadProfileSpec, profileSpec)
                && profileSpec.getDeviceObisCode().equals(loadProfileSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forLoadProfileSpec(thesaurus, this, loadProfileSpec.getDeviceObisCode(), loadProfileSpec);
            }
        }
    }

    private void validateUniqueLoadProfileType(LoadProfileSpec loadProfileSpec) {
        for (LoadProfileSpec profileSpec : loadProfileSpecs) {
            if (profileSpec.getLoadProfileType().getId() == loadProfileSpec.getLoadProfileType().getId()) {
                throw new DuplicateLoadProfileTypeException(thesaurus, this, loadProfileSpec.getLoadProfileType(), loadProfileSpec);
            }
        }
    }

    @Override
    public void deleteLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forLoadProfileSpec(this.thesaurus, loadProfileSpec, this);
        }
        loadProfileSpec.validateDelete();
        this.loadProfileSpecs.remove(loadProfileSpec);
        this.eventService.postEvent(EventType.DEVICETYPE_DELETED.topic(),loadProfileSpec);
    }

    @Override
    public List<LogBookSpec> getLogBookSpecs() {
        return Collections.unmodifiableList(this.logBookSpecs);
    }

    @Override
    public LogBookSpec.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType) {
        return new LogBookSpecBuilderForConfig(logBookSpecProvider, this, logBookType);
    }

    private class LogBookSpecBuilderForConfig extends LogBookSpecImpl.LogBookSpecBuilder {

        LogBookSpecBuilderForConfig(Provider<LogBookSpecImpl> logBookSpecProvider, DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
            super(logBookSpecProvider, deviceConfiguration, logBookType);
        }

        @Override
        public LogBookSpecImpl add() {
            LogBookSpecImpl logBookSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewLogBookSpec(getThesaurus()));
            validateUniqueLogBookType(logBookSpec);
            validateUniqueLogBookObisCode(logBookSpec);
            DeviceConfigurationImpl.this.logBookSpecs.add(logBookSpec);
            return logBookSpec;
        }
    }

    private void validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException specException) {
        if (isActive()) {
            throw specException;
        }
    }

    @Override
    public LogBookSpec.LogBookSpecUpdater getLogBookSpecUpdaterFor(LogBookSpec logBookSpec) {
        return new LogBookSpecUpdaterForConfig((LogBookSpecImpl) logBookSpec);
    }

    private class LogBookSpecUpdaterForConfig extends LogBookSpecImpl.LogBookSpecUpdater {

        private LogBookSpecUpdaterForConfig(LogBookSpecImpl logBookSpec) {
            super(logBookSpec);
        }

        @Override
        public void update() {
            validateUniqueLogBookObisCode(logBookSpec);
            super.update();
        }
    }

    private void validateUniqueLogBookType(LogBookSpecImpl logBookSpec) {
        for (LogBookSpec spec : logBookSpecs) {
            if (spec.getLogBookType().getId() == logBookSpec.getLogBookType().getId()) {
                throw new DuplicateLogBookTypeException(thesaurus, this, logBookSpec.getLogBookType(), logBookSpec);
            }
        }
    }

    private void validateUniqueLogBookObisCode(LogBookSpec logBookSpec) {
        for (LogBookSpec bookSpec : logBookSpecs) {
            if (!isSameIdObject(bookSpec, logBookSpec)
                    && bookSpec.getDeviceObisCode().equals(logBookSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forLogBookSpec(thesaurus, this, logBookSpec.getDeviceObisCode(), logBookSpec);
            }
        }
    }

    private boolean isSameIdObject(HasId first, HasId second) {
        return first.getId() == second.getId();
    }

    public void deleteLogBookSpec(LogBookSpec logBookSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forLogbookSpec(this.thesaurus, logBookSpec, this);
        }
        logBookSpec.validateDelete();
        this.logBookSpecs.remove(logBookSpec);
        this.eventService.postEvent(EventType.DEVICETYPE_DELETED.topic(),logBookSpec);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
//        if (getBaseFactory().hasAnyForDeviceConfig(this)) {
//            throw new BusinessException("cannotDeactivateDeviceConfig",
//                    "Device configuration '{0}' cannot be deactivated since it is still in use", this.getName());
//        }
        //TODO need to check if there are devices who are modeled by this DeviceConfiguration (JP-906)
        this.active = false;
    }

    @Override
    public void save() {
        this.modificationDate = this.clock.now();
        super.save();
    }

    @Override
    protected void postNew() {
        validateRequiredFields();
        super.postNew();
    }

    private void validateRequiredFields() {
        validateDeviceTypeExists();
    }

    private void validateDeviceTypeExists() {
        if (!this.deviceType.isPresent()) {
            throw new DeviceTypeIsRequiredException(this.thesaurus);
        }
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICECONFIGURATION;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICECONFIGURATION;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICECONFIGURATION;
    }

    @Override
    protected void validateDelete() {
        if (isActive()) {
            throw new DeviceConfigurationIsActiveException(this.thesaurus, this);
        }
    }

    public boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId) {
        if (getLogBookSpecs() != null && !getLogBookSpecs().isEmpty()) {
            for (LogBookSpec logBookSpec : getLogBookSpecs()) {
                if (logBookSpec.getLogBookType().getId() == logBookTypeId && logBookSpec.getId() != updateId) {
                    return true;
                }
            }
        }
        return false;
    }
}