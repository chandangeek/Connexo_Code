package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateIntervalWhenLoadProfileTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLoadProfileTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ObisCodeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingAlreadyInLoadProfileTypeException;
import com.energyict.mdc.device.config.exceptions.UnsupportedIntervalException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

/**
 * Copyrights EnergyICT
 * Date: 11-jan-2011
 * Time: 16:05:54
 */
public class LoadProfileTypeImpl extends PersistentNamedObject<LoadProfileType> implements LoadProfileType {

    private DeviceConfigurationService deviceConfigurationService;
    private String obisCodeString;
    private ObisCode obisCode;
    private TimeDuration interval;
    private String description;
    private Date modificationDate;
    private List<LoadProfileTypeRegisterMappingUsage> registerMappingUsages = new ArrayList<>();

    private Clock clock;

    @Inject
    public LoadProfileTypeImpl(DataModel dataModel, EventService eventService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus, Clock clock) {
        super(LoadProfileType.class, dataModel, eventService, thesaurus);
        this.deviceConfigurationService = deviceConfigurationService;
        this.clock = clock;
    }

    LoadProfileTypeImpl initialize(String name, ObisCode obisCode, TimeDuration interval) {
        this.setName(name);
        this.setObisCode(obisCode);
        this.setInterval(interval);
        return this;
    }

    static LoadProfileTypeImpl from (DataModel dataModel, String name, ObisCode obisCode, TimeDuration interval) {
        return dataModel.getInstance(LoadProfileTypeImpl.class).initialize(name, obisCode, interval);
    }

    @Override
    public void save () {
        this.modificationDate = this.clock.now();
        super.save();
    }

    /**
     * Saves this object for the first time.
     */
    protected void postNew() {
        this.getDataMapper().persist(this);
    }

    /**
     * Updates the changes made to this object.
     */
    protected void post() {
        this.validateDeviceConfigurations();
        this.getDataMapper().update(this);
    }

    private void validateDeviceConfigurations() {
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingLoadProfileType(this);
        for (DeviceConfiguration each : deviceConfigurations) {
            ServerDeviceConfiguration deviceConfiguration = (ServerDeviceConfiguration) each;
            deviceConfiguration.validateUpdateLoadProfileType(this);
        }
    }

    @Override
    protected NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus) {
        throw NameIsRequiredException.loadProfileTypeNameIsRequired(thesaurus);
    }

    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    public ObisCode getObisCode() {
        if (this.obisCode == null) {
            this.obisCode = ObisCode.fromString(this.obisCodeString);
        }
        return this.obisCode;
    }

    @Override
    public void setObisCode(ObisCode obisCode) {
        if (obisCode == null) {
            throw ObisCodeIsRequiredException.loadProfileTypeRequiresObisCode(this.getThesaurus());
        }
        if (this.isInUse()) {
            throw new CannotUpdateObisCodeWhenLoadProfileTypeIsInUseException(this.getThesaurus(), this);
        }
        this.obisCodeString = obisCode.toString();
        this.obisCode = obisCode;
    }

    @Override
    public TimeDuration getInterval() {
        return interval;
    }

    @Override
    public void setInterval(TimeDuration interval) {
        if (!is(this.interval).equalTo(interval)) {
            if (this.isInUse()) {
                throw new CannotUpdateIntervalWhenLoadProfileTypeIsInUseException(this.getThesaurus(), this);
            }
        }
        if (interval == null || interval.isEmpty()) {
            throw IntervalIsRequiredException.forLoadProfileType(getThesaurus());
        }
        if ((interval.getTimeUnitCode() == TimeDuration.WEEKS)) {
            throw UnsupportedIntervalException.weeksAreNotSupportedForLoadProfileTypes(this.getThesaurus(), this);
        }
        if (countMustBeOneFor(interval) && interval.getCount() != 1) {
            throw UnsupportedIntervalException.multipleNotSupported(this.getThesaurus(), interval);
        }
        if ((interval.getCount() <= 0)) {
            throw UnsupportedIntervalException.strictlyPositive(this.getThesaurus(), interval);
        }
        this.interval = interval;
    }

    private boolean countMustBeOneFor(TimeDuration interval) {
        return interval.getTimeUnitCode() == TimeDuration.DAYS || interval.getTimeUnitCode() == TimeDuration.MONTHS || interval.getTimeUnitCode() == TimeDuration.YEARS;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public List<RegisterMapping> getRegisterMappings() {
        List<RegisterMapping> registerMappings = new ArrayList<>(this.registerMappingUsages.size());
        for (LoadProfileTypeRegisterMappingUsage registerMappingUsage : this.registerMappingUsages) {
            registerMappings.add(registerMappingUsage.getRegisterMapping());
        }
        return registerMappings;
    }

    @Override
    public void addRegisterMapping(RegisterMapping registerMapping) {
        for (LoadProfileTypeRegisterMappingUsage registerMappingUsage : this.registerMappingUsages) {
            if (registerMappingUsage.sameRegisterMapping(registerMapping)) {
                throw new RegisterMappingAlreadyInLoadProfileTypeException(this.getThesaurus(), this, registerMapping);
            }
        }
        this.registerMappingUsages.add(new LoadProfileTypeRegisterMappingUsage(this, registerMapping));
    }

    @Override
    public void removeRegisterMapping(RegisterMapping registerMapping) {
        Iterator<LoadProfileTypeRegisterMappingUsage> iterator = this.registerMappingUsages.iterator();
        while (iterator.hasNext()) {
            LoadProfileTypeRegisterMappingUsage registerMappingUsage = iterator.next();
            if (registerMappingUsage.sameRegisterMapping(registerMapping)) {
                /* Todo: Legacy code validated that there were no Channels that used the mapping
                 * by calling ChannelFactory#hasChannelsForLoadProfileTypeAndMapping(this, registerMapping).
                 * This will now have to be dealt with via events. */
                this.validateNoChannelSpecForReqisterMapping(registerMapping);
                iterator.remove();
            }
        }
    }

    private void validateNoChannelSpecForReqisterMapping(RegisterMapping registerMapping) {
        List<ChannelSpec> channelSpecs = this.deviceConfigurationService.findChannelSpecsForRegisterMappingInLoadProfileType(registerMapping, this);
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByChannelSpecs(this.thesaurus, registerMapping, channelSpecs);
        }
    }

    @Override
    protected void validateDelete() {
        List<DeviceTypeLoadProfileTypeUsage> loadProfileTypeUsages = this.mapper(DeviceTypeLoadProfileTypeUsage.class).find("loadProfileType", this);
        if (!loadProfileTypeUsages.isEmpty()) {
            List<DeviceType> deviceTypes = new ArrayList<>(loadProfileTypeUsages.size());
            for (DeviceTypeLoadProfileTypeUsage loadProfileTypeUsage : loadProfileTypeUsages) {
                deviceTypes.add(loadProfileTypeUsage.getDeviceType());
            }
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUseByDeviceType(this.getThesaurus(), this, deviceTypes);
        }
        List<LoadProfileSpec> loadProfileSpecs = this.mapper(LoadProfileSpec.class).find("loadProfileType", this);
        if (!loadProfileSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUseByLoadProfileSpec(this.getThesaurus(), this, loadProfileSpecs);
        }
    }

    @Override
    public boolean isInUse() {
        return this.inUseByLoadProfileSpecs();
    }

    private boolean inUseByLoadProfileSpecs() {
        DataMapper<LoadProfileSpec> mapper = this.mapper(LoadProfileSpec.class);
        List<LoadProfileSpec> loadProfileSpecs = mapper.find("loadProfileType", this);
        return !loadProfileSpecs.isEmpty();
    }

}