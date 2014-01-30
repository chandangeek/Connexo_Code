package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateIntervalWhenLoadProfileTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLoadProfileTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ObisCodeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingAlreadyInLoadProfileTypeException;
import com.energyict.mdc.device.config.exceptions.UnsupportedIntervalException;
import com.energyict.mdc.pluggable.impl.EventType;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.elster.jupiter.util.Checks.*;

/**
 * Copyrights EnergyICT
 * Date: 11-jan-2011
 * Time: 16:05:54
 */
public class LoadProfileTypeImpl implements LoadProfileType {

    private long id;
    @NotNull
    private String name;
    private String obisCodeString;
    private ObisCode obisCode;
    private TimeDuration interval;
    private String description;
    private Date modificationDate;
    private List<LoadProfileTypeRegisterMappingUsage> registerMappingUsages = new ArrayList<>();

    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;
    private Clock clock;

    @Inject
    public LoadProfileTypeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
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

    private DataMapper<LoadProfileType> getDataMapper() {
        return this.dataModel.mapper(LoadProfileType.class);
    }

    @Override
    public void save () {
        this.modificationDate = this.clock.now();
        if (this.id > 0) {
            this.post();
        }
        else {
            this.postNew();
        }
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
        /* Todo: find all DeviceConfigurations that use this mapping via LoadProfileSpec
         *       and validate that the changes applied to this LoadProfileType
         *       do not violate any device configuration business constraints. */
    }

    public void delete() {
        this.validateDelete();
        this.notifyDependents();
        this.getDataMapper().remove(this);
    }

    private void notifyDependents() {
        this.eventService.postEvent(EventType.DELETED.topic(), this);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.validateName(name);
        if (!name.equals(this.getName())) {
            this.validateUniqueName(name);
        }
        this.name = name;
    }

    private void validateName(String newName) {
        if (newName == null) {
            throw NameIsRequiredException.loadProfileTypeNameIsRequired(this.thesaurus);
        }
        if (newName.trim().isEmpty()) {
            throw NameIsRequiredException.loadProfileTypeNameIsRequired(this.thesaurus);
        }
    }

    private void validateUniqueName(String name) {
        if (this.findOtherByName(name) != null) {
            throw DuplicateNameException.loadProfileTypeAlreadyExists(this.thesaurus, name);
        }
    }

    private LoadProfileType findOtherByName(String name) {
        return this.getDataMapper().getUnique("name", name).orNull();
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
            throw new ObisCodeIsRequiredException(this.thesaurus);
        }
        if (this.isInUse()) {
            throw new CannotUpdateObisCodeWhenLoadProfileTypeIsInUseException(this.thesaurus, this);
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
                throw new CannotUpdateIntervalWhenLoadProfileTypeIsInUseException(this.thesaurus, this);
            }
        }
        if (interval == null || interval.isEmpty()) {
            throw new IntervalIsRequiredException(this.thesaurus);
        }
        if ((interval.getTimeUnitCode() == TimeDuration.WEEKS)) {
            throw UnsupportedIntervalException.weeks(this.thesaurus);
        }
        if (countMustBeOneFor(interval) && interval.getCount() != 1) {
            throw UnsupportedIntervalException.multipleNotSupported(this.thesaurus, interval);
        }
        if ((interval.getCount() <= 0)) {
            throw UnsupportedIntervalException.strictlyPositive(this.thesaurus, interval);
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
            registerMappings.add(registerMappingUsage.registerMapping);
        }
        return registerMappings;
    }

    @Override
    public void addRegisterMapping(RegisterMapping registerMapping) {
        for (LoadProfileTypeRegisterMappingUsage registerMappingUsage : this.registerMappingUsages) {
            if (registerMappingUsage.registerMapping.getId() == registerMapping.getId()) {
                throw new RegisterMappingAlreadyInLoadProfileTypeException(this.thesaurus, this, registerMapping);
            }
        }
        this.registerMappingUsages.add(new LoadProfileTypeRegisterMappingUsage(this, registerMapping));
    }

    @Override
    public void removeRegisterMapping(RegisterMapping registerMapping) {
        Iterator<LoadProfileTypeRegisterMappingUsage> iterator = this.registerMappingUsages.iterator();
        while (iterator.hasNext()) {
            LoadProfileTypeRegisterMappingUsage registerMappingUsage = iterator.next();
            if (registerMappingUsage.registerMapping.getId() == registerMapping.getId()) {
                /* Legacy code validated that there were not Channels that used the mapping
                 * by calling ChannelFactory#hasChannelsForLoadProfileTypeAndMapping(this, registerMapping).
                 * This will now have to be dealt with via events. */
                /* Todo: Check that no ChannelSpec is using the RegisterMapping.
                 *  see: ChannelSpecFactory#hasAnyForLoadProfileTypeAndRegisterMapping(this, registerMapping). */
                 iterator.remove();
            }
        }
    }

    @Override
    protected void validateDelete() {
        List<DeviceTypeLoadProfileTypeUsage> loadProfileTypeUsages = this.dataModel.mapper(DeviceTypeLoadProfileTypeUsage.class).find("loadProfileType", this);
        if (!loadProfileTypeUsages.isEmpty()) {
            List<DeviceType> deviceTypes = new ArrayList<>(loadProfileTypeUsages.size());
            for (DeviceTypeLoadProfileTypeUsage loadProfileTypeUsage : loadProfileTypeUsages) {
                deviceTypes.add(loadProfileTypeUsage.deviceType);
            }
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUse(this.thesaurus, this, deviceTypes);
        }
        List<LoadProfileSpec> loadProfileSpecs = this.dataModel.mapper(LoadProfileSpec.class).find("loadProfileType", this);
        if (!loadProfileSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUse(this.thesaurus, this, loadProfileSpecs);
        }
    }

    @Override
    public boolean isInUse() {
        return this.inUseByLoadProfileSpecs();
    }

    private boolean inUseByLoadProfileSpecs() {
        DataMapper<LoadProfileSpec> mapper = this.dataModel.mapper(LoadProfileSpec.class);
        List<LoadProfileSpec> loadProfileSpecs = mapper.find("loadProfileType", this);
        return !loadProfileSpecs.isEmpty();
    }

}