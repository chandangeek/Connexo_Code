package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.exceptions.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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

    enum Fields {
           OBIS_CODE("obisCode");
           private final String javaFieldName;

           Fields(String javaFieldName) {
               this.javaFieldName = javaFieldName;
           }

           String fieldName() {
               return javaFieldName;
           }
       }

    private DeviceConfigurationService deviceConfigurationService;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED_KEY + "}")
    private String obisCode;
    private ObisCode obisCodeCached;
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

    protected void post() {
        this.validateDeviceConfigurations();
        super.post();
    }

    private void validateDeviceConfigurations() {
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingLoadProfileType(this);
        for (DeviceConfiguration each : deviceConfigurations) {
            ServerDeviceConfiguration deviceConfiguration = (ServerDeviceConfiguration) each;
            deviceConfiguration.validateUpdateLoadProfileType(this);
        }
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.loadProfileTypeAlreadyExists(thesaurus, name);
    }

    protected void doDelete() {
        this.registerMappingUsages.clear();
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.LOADPROFILETYPE;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.LOADPROFILETYPE;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.LOADPROFILETYPE;
    }

    public ObisCode getObisCode() {
        if (this.obisCodeCached == null) {
            this.obisCodeCached = ObisCode.fromString(this.obisCode);
        }
        return this.obisCodeCached;
    }

    @Override
    public void setObisCode(ObisCode obisCode) {
        if (obisCode == null) {
            // javax.validation will throw ConstraintValidationException in the end
            this.obisCode = null;
            this.obisCodeCached = null;
        }
        else {
            if (this.isInUse()) {
                throw new CannotUpdateObisCodeWhenLoadProfileTypeIsInUseException(this.getThesaurus(), this);
            }
            this.obisCode = obisCode.toString();
            this.obisCodeCached = obisCode;
        }
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
                this.validateNoChannelSpecForRegisterMapping(registerMapping);
                iterator.remove();
            }
        }
    }

    private void validateNoChannelSpecForRegisterMapping(RegisterMapping registerMapping) {
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