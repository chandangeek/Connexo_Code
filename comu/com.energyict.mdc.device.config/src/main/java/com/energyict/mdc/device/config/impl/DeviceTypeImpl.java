package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceCommunicationFunction;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceUsageType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.DeviceProtocolIsRequiredException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class DeviceTypeImpl extends PersistentNamedObject<DeviceType> implements DeviceType {

    private int channelCount;
    private String description;
    private boolean channelJournalUsed;
    private DeviceUsageType deviceUsageType;
    private int communicationFunctionMask;
    private List<DeviceConfiguration> deviceConfigurations = new ArrayList<>();
    private List<DeviceTypeLogBookTypeUsage> logBookTypeUsages = new ArrayList<>();
    private List<DeviceTypeLoadProfileTypeUsage> loadProfileTypeUsages = new ArrayList<>();
    private List<DeviceTypeRegisterMappingUsage> registerMappingUsages = new ArrayList<>();

    private long deviceProtocolPluggableClassId;
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private ProtocolPluggableService protocolPluggableService;
    /**
     * The DeviceProtocol of this DeviceType, only for local usage
     */
    private DeviceProtocol localDeviceProtocol;

    @Inject
    public DeviceTypeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ProtocolPluggableService protocolPluggableService) {
        super(DeviceType.class, dataModel, eventService, thesaurus);
        this.protocolPluggableService = protocolPluggableService;
    }

    DeviceTypeImpl initialize(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.setName(name);
        this.setDeviceProtocolPluggableClass(deviceProtocolPluggableClass);
        return this;
    }

    static DeviceTypeImpl from (DataModel dataModel, String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        return dataModel.getInstance(DeviceTypeImpl.class).initialize(name, deviceProtocolPluggableClass);
    }

    @Override
    protected NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus) {
        return NameIsRequiredException.deviceTypeNameIsRequired(thesaurus);
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

    private void notifyDelete (ServerDeviceConfiguration deviceConfiguration) {
        deviceConfiguration.notifyDelete();
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

    public int getChannelCount() {
        return this.channelCount;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String newDescription) {
        this.description = newDescription;
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
    public void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        if (deviceProtocolPluggableClass == null) {
            throw new DeviceProtocolIsRequiredException(this.getThesaurus());
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
            logBookTypes.add(logBookTypeUsage.logBookType);
        }
        return logBookTypes;
    }

    @Override
    public List<RegisterMapping> getRegisterMappings() {
        List<RegisterMapping> registerMappings = new ArrayList<>(this.registerMappingUsages.size());
        for (DeviceTypeRegisterMappingUsage registerMappingUsage : this.registerMappingUsages) {
            registerMappings.add(registerMappingUsage.registerMapping);
        }
        return registerMappings;
    }

    @Override
    public List<LoadProfileType> getLoadProfileTypes() {
        List<LoadProfileType> loadProfileTypes = new ArrayList<>(this.loadProfileTypeUsages.size());
        for (DeviceTypeLoadProfileTypeUsage loadProfileTypeUsage : this.loadProfileTypeUsages) {
            loadProfileTypes.add(loadProfileTypeUsage.loadProfileType);
        }
        return loadProfileTypes;
    }

    @Override
    public void addLoadProfileType(LoadProfileType loadProfileType) {

    }

    @Override
    public void removeLoadProfileType(LoadProfileType loadProfileType) {

    }

    public boolean supportsMessaging() {
        return this.getDeviceProtocolPluggableClass() != null;
    }

    public boolean isChannelJournalUsed() {
        return this.channelJournalUsed;
    }

    public boolean isLogicalSlave() {
        if (this.getDeviceProtocolPluggableClass() != null) {
            final List<DeviceProtocolCapabilities> deviceProtocolCapabilities = getLocalDeviceProtocol().getDeviceProtocolCapabilities();
            return deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_SLAVE) && deviceProtocolCapabilities.size() == 1;
        } else {
            return false;
        }
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

    public boolean hasCommunicationFunction(DeviceCommunicationFunction function) {
        return (getCommunicationFunctionMask() & function.getCode()) != 0;
    }

    public List<DeviceConfiguration> getConfigurations() {
        return this.deviceConfigurations;
    }

}