package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceCommunicationConfigurationFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigurationIsActiveException;
import com.energyict.mdc.device.config.exceptions.DeviceTypeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.DuplicateLoadProfileTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateLogBookTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.protocol.api.device.Device;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User: gde
 * Date: 5/11/12
 */
public class DeviceConfigurationImpl extends PersistentNamedObject<DeviceConfiguration> implements DeviceConfiguration, ServerDeviceConfiguration {

    private String description;

    private boolean active;
    private Device prototype;

    private DeviceType deviceType;
    private List<RegisterSpec> registerSpecs = new ArrayList<>();
    private List<ChannelSpec> channelSpecs = new ArrayList<>();
    private List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>();
    private List<LogBookSpec> logBookSpecs = new ArrayList<>();
    private DeviceCommunicationConfiguration communicationConfiguration;
    private Date modificationDate;
    private Clock clock;
    private final Provider<LoadProfileSpecImpl> loadProfileSpecProvider;
    private final Provider<RegisterSpecImpl> registerSpecProvider;
    private final Provider<LogBookSpecImpl> logBookSpecProvider;
    private final Provider<ChannelSpecImpl> channelSpecProvider;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    protected DeviceConfigurationImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock,
                                      Provider<LoadProfileSpecImpl> loadProfileSpecProvider,
                                      Provider<RegisterSpecImpl> registerSpecProvider,
                                      Provider<LogBookSpecImpl> logBookSpecProvider,
                                      Provider<ChannelSpecImpl> channelSpecProvider,
                                      DeviceConfigurationService deviceConfigurationService) {
        super(DeviceConfiguration.class, dataModel, eventService, thesaurus);
        this.clock = clock;

        this.loadProfileSpecProvider = loadProfileSpecProvider;
        this.registerSpecProvider = registerSpecProvider;
        this.logBookSpecProvider = logBookSpecProvider;
        this.channelSpecProvider = channelSpecProvider;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    DeviceConfigurationImpl initialize(DeviceType deviceType, String name){
        this.deviceType = deviceType;
        setName(name);
        return this;
    }


    @Override
    public DeviceCommunicationConfiguration getCommunicationConfiguration() {
        if (this.communicationConfiguration == null) {
            List<DeviceCommunicationConfigurationFactory> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceCommunicationConfigurationFactory.class);
            if (modulesImplementing.size() > 0) {
                this.communicationConfiguration = modulesImplementing.get(0).findFor(this);
            }
        }
        return this.communicationConfiguration;
    }

    public String getDescription() {
        return this.description;
    }

    public int getPrototypeId() {
        return getPrototypeDevice() == null ? 0 : getPrototypeDevice().getId();
    }

    public Device getPrototypeDevice() {
        return this.prototype;
    }

    @Override
    protected NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus) {
        return NameIsRequiredException.deviceConfigurationNameIsRequired(this.thesaurus);
    }

    @Override
    public void notifyDelete() {
        //TODO
    }

    //TODO the creation of the CommunicationConfiguration is currently skipped ...
//    protected void doInit(DeviceConfigurationShadow shadow) throws SQLException, BusinessException {
//        copyNew(shadow);
//        validateNew(shadow);
//        postNew();
//        if (DeviceCollectionMethodType.COMSERVER.equals(getDeviceType().getDeviceCollectionMethodType())) {
//            // Will be null during import process since a separate Command needs to process that
//            if (shadow.getCommunicationConfigurationShadow() != null) {
//                this.createCommunicationConfiguration(shadow);
//            }
//        }
//
//        createLoadProfileSpecs(shadow.getLoadProfileSpecShadows());
//        createChannelSpecs(shadow.getChannelSpecShadows().getNewShadows());
//        createLogBookSpecs(shadow.getLogBookSpecShadows());
//        createRegisterSpecs(shadow.getRegisterSpecShadows().getNewShadows());
//        created();
//    }


    @Override
    protected void validateName(String newName) {
        super.validateName(newName);
        DeviceConfiguration deviceConfiguration = this.deviceConfigurationService.findDeviceConfigurationByNameAndDeviceType(newName, deviceType);
        if (deviceConfiguration != null) {
            throw DuplicateNameException.deviceConfigurationExists(thesaurus, newName);
        }
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public List<RegisterSpec> getRegisterSpecs() {
        return Collections.unmodifiableList(this.registerSpecs);
    }

    @Override
    public RegisterSpecImpl.RegisterSpecBuilder createRegisterSpec(RegisterMapping registerMapping) {
        return new RegisterSpecBuilderForConfig(registerSpecProvider, this, registerMapping);
    }

    class RegisterSpecBuilderForConfig extends RegisterSpecImpl.RegisterSpecBuilder {

        public RegisterSpecBuilderForConfig(Provider<RegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping) {
            super(registerSpecProvider, deviceConfiguration, registerMapping);
        }

        @Override
        public RegisterSpec add() {
            RegisterSpec registerSpec = super.add();
            validateUniqueRegisterSpecObisCode(registerSpec);
            DeviceConfigurationImpl.this.registerSpecs.add(registerSpec);
            return registerSpec;
        }
    }

    private void validateUniqueRegisterSpecObisCode(RegisterSpec registerSpec) {
        for (RegisterSpec spec : registerSpecs) {
            if (spec.getDeviceObisCode().equals(registerSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forRegisterSpec(thesaurus, this, registerSpec.getDeviceObisCode(), registerSpec);
            }
        }
    }

    public void deleteRegisterSpec(RegisterSpec registerSpec) {
        // TODO COmplete!!!

//        if (getActive() && !shadow.getRegisterSpecShadows().getDeletedShadows().isEmpty()) {
//            throw new BusinessException("deleteRegisterSpecsFromActiveDeviceConfigIsNotAllowed",
//                    "It's not allowed to delete register specifications of an active device configuration");
//        }
        if (getActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.canNotDeleteRegisterSpec(this.thesaurus, this, registerSpec);
        }
        registerSpec.validateDelete();
        this.registerSpecs.remove(registerSpec);
        this.eventService.postEvent(EventType.DELETED.topic(),registerSpec);
    }

    public List<ChannelSpec> getChannelSpecs() {
        return Collections.unmodifiableList(this.channelSpecs);
    }

    @Override
    public ChannelSpecImpl.ChannelSpecBuilder createChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
        return new ChannelSpecBuilderForConfig(channelSpecProvider, this, registerMapping, phenomenon, loadProfileSpec);
    }

    class ChannelSpecBuilderForConfig extends ChannelSpecImpl.ChannelSpecBuilder {

        public ChannelSpecBuilderForConfig(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
            super(channelSpecProvider, deviceConfiguration, registerMapping, phenomenon, loadProfileSpec);
        }

        @Override
        public ChannelSpec add() {
            ChannelSpec channelSpec = super.add();
            validateUniqueChannelSpecPerLoadProfileSpec(channelSpec);
            DeviceConfigurationImpl.this.channelSpecs.add(channelSpec);
            return channelSpec;
        }
    }

    private void validateUniqueChannelSpecPerLoadProfileSpec(ChannelSpec channelSpec) {
        for (ChannelSpec spec : channelSpecs) {
            if (channelSpec.getLoadProfileSpec() == null) {
                if (spec.getLoadProfileSpec() == null && channelSpec.getDeviceObisCode().equals(spec.getDeviceObisCode())) {
                    throw DuplicateObisCodeException.forChannelSpecConfigWithoutLoadProfileSpec(thesaurus, this, channelSpec.getDeviceObisCode(), channelSpec);
                }
            } else if (channelSpec.getLoadProfileSpec().equals(spec.getLoadProfileSpec())) {
                if (channelSpec.getDeviceObisCode().equals(spec.getDeviceObisCode())) {
                    throw DuplicateObisCodeException.forChannelSpecInLoadProfileSpec(thesaurus, this, channelSpec.getDeviceObisCode(), channelSpec, channelSpec.getLoadProfileSpec());
                }
            }
        }
    }

    public void deleteChannelSpec(ChannelSpec channelSpec) {
        if (getActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forChannelSpec(this.thesaurus, channelSpec, this);
        }
        channelSpec.validateDelete();
        this.channelSpecs.remove(channelSpec);
        this.eventService.postEvent(EventType.DELETED.topic(),channelSpec);
    }

    public List<LoadProfileSpec> getLoadProfileSpecs() {
        return Collections.unmodifiableList(this.loadProfileSpecs);
    }

    @Override
    public LoadProfileSpecImpl.LoadProfileSpecBuilder createLoadProfileSpec(LoadProfileType loadProfileType) {
        return new LoadProfileSpecBuilderForConfig(loadProfileSpecProvider, this, loadProfileType);
    }

    class LoadProfileSpecBuilderForConfig extends LoadProfileSpecImpl.LoadProfileSpecBuilder {

        LoadProfileSpecBuilderForConfig(Provider<LoadProfileSpecImpl> loadProfileSpecProvider, DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType) {
            super(loadProfileSpecProvider, deviceConfiguration, loadProfileType);
        }

        public LoadProfileSpec add() {
            LoadProfileSpec loadProfileSpec = super.add();
            validateUniqueLoadProfileType(loadProfileSpec);
            validateUniqueLoadProfileObisCode(loadProfileSpec);
            DeviceConfigurationImpl.this.loadProfileSpecs.add(loadProfileSpec);
            return loadProfileSpec;
        }
    }

    private void validateUniqueLoadProfileObisCode(LoadProfileSpec loadProfileSpec) {
        for (LoadProfileSpec profileSpec : loadProfileSpecs) {
            if (profileSpec.getDeviceObisCode().equals(loadProfileSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forLoadProfileSpec(thesaurus, this, loadProfileSpec.getDeviceObisCode(), loadProfileSpec);
            }
        }
    }

    private void validateUniqueLoadProfileType(LoadProfileSpec loadProfileSpec) {
        for (LoadProfileSpec profileSpec : loadProfileSpecs) {
            if (profileSpec.getLoadProfileType().equals(loadProfileSpec.getLoadProfileType())) {
                throw new DuplicateLoadProfileTypeException(thesaurus, this, loadProfileSpec.getLoadProfileType(), loadProfileSpec);
            }
        }
    }

    @Override
    public void deleteLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        if (getActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forLoadProfileSpec(this.thesaurus, loadProfileSpec, this);
        }
        loadProfileSpec.validateDelete();
        this.loadProfileSpecs.remove(loadProfileSpec);
        this.eventService.postEvent(EventType.DELETED.topic(),loadProfileSpec);
    }

    @Override
    public List<LogBookSpec> getLogBookSpecs() {
        return Collections.unmodifiableList(this.logBookSpecs);
    }

    @Override
    public LogBookSpecImpl.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType) {
        return new LogBookSpecBuilderForConfig(logBookSpecProvider, this, logBookType);
    }

    class LogBookSpecBuilderForConfig extends LogBookSpecImpl.LogBookSpecBuilder {

        LogBookSpecBuilderForConfig(Provider<LogBookSpecImpl> logBookSpecProvider, DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
            super(logBookSpecProvider, deviceConfiguration, logBookType);
        }

        @Override
        public LogBookSpecImpl add() {
            LogBookSpecImpl logBookSpec = super.add();
            validateUniqueLogBookType(logBookSpec);
            validateUniqueLogBookObisCode(logBookSpec);
            DeviceConfigurationImpl.this.logBookSpecs.add(logBookSpec);
            return logBookSpec;
        }
    }

    private void validateUniqueLogBookType(LogBookSpecImpl logBookSpec) {
        for (LogBookSpec spec : logBookSpecs) {
            if (spec.getLogBookType().equals(logBookSpec.getLogBookType())) {
                throw new DuplicateLogBookTypeException(thesaurus, this, logBookSpec.getLogBookType(), logBookSpec);
            }
        }
    }

    private void validateUniqueLogBookObisCode(LogBookSpec logBookSpec) {
        for (LogBookSpec bookSpec : logBookSpecs) {
            if (bookSpec.getDeviceObisCode().equals(logBookSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forLogBookSpec(thesaurus, this, logBookSpec.getDeviceObisCode(), logBookSpec);
            }
        }
    }

    public void deleteLogBookSpec(LogBookSpec logBookSpec) {
        if (getActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forLogbookSpec(this.thesaurus, logBookSpec, this);
        }
        logBookSpec.validateDelete();
        this.logBookSpecs.remove(logBookSpec);
        this.eventService.postEvent(EventType.DELETED.topic(),logBookSpec);
    }

    public boolean getActive() {
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
        this.getDataMapper().persist(this);
    }

    private void validateRequiredFields() {
        validateDeviceTypeExists();
    }

    private void validateDeviceTypeExists() {
        if (this.deviceType == null) {
            throw new DeviceTypeIsRequiredException(this.thesaurus);
        }
    }

    @Override
    protected void post() {
        this.getDataMapper().update(this);
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected void validateDelete() {
        if (getActive()) {
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