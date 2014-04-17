package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeRegisterMappingUsage;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.exceptions.RegisterMappingAlreadyInLoadProfileTypeException;
import com.energyict.mdc.masterdata.exceptions.UnsupportedIntervalException;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11-jan-2011
 * Time: 16:05:54
 */
public class LoadProfileTypeImpl extends PersistentNamedObject<LoadProfileType> implements LoadProfileType, PersistenceAware {

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

    private final MasterDataService masterDataService;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED + "}")
    private String obisCode;
    private String oldObisCode;
    private ObisCode obisCodeCached;
    private TimeDuration interval;
    private long oldIntervalSeconds;
    private String description;
    private Date modificationDate;
    private List<LoadProfileTypeRegisterMappingUsageImpl> registerMappingUsages = new ArrayList<>();

    private Clock clock;

    @Inject
    public LoadProfileTypeImpl(DataModel dataModel, EventService eventService, MasterDataService masterDataService, Thesaurus thesaurus, Clock clock) {
        super(LoadProfileType.class, dataModel, eventService, thesaurus);
        this.masterDataService = masterDataService;
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
    public void postLoad() {
        this.synchronizeOldValues();
    }

    @Override
    public void save () {
        this.modificationDate = this.clock.now();
        super.save();
        this.synchronizeOldValues();
    }

    private void synchronizeOldValues() {
        this.oldObisCode = this.obisCode;
        if (this.interval == null) {
            this.oldIntervalSeconds = 0;
        }
        else {
            this.oldIntervalSeconds = this.interval.getSeconds();
        }
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
            this.obisCode = obisCode.toString();
            this.obisCodeCached = obisCode;
        }
    }

    // Used by EventType
    public String getOldObisCode() {
        return oldObisCode;
    }

    @Override
    public TimeDuration getInterval() {
        return interval;
    }

    @Override
    public void setInterval(TimeDuration interval) {
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

    // Used by EventType
    public long getOldIntervalSeconds() {
        return oldIntervalSeconds;
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
        for (LoadProfileTypeRegisterMappingUsageImpl registerMappingUsage : this.registerMappingUsages) {
            if (registerMappingUsage.sameRegisterMapping(registerMapping)) {
                throw new RegisterMappingAlreadyInLoadProfileTypeException(this.getThesaurus(), this, registerMapping);
            }
        }
        this.registerMappingUsages.add(new LoadProfileTypeRegisterMappingUsageImpl(this, registerMapping));
    }

    @Override
    public void removeRegisterMapping(RegisterMapping registerMapping) {
        Iterator<LoadProfileTypeRegisterMappingUsageImpl> iterator = this.registerMappingUsages.iterator();
        while (iterator.hasNext()) {
            LoadProfileTypeRegisterMappingUsageImpl registerMappingUsage = iterator.next();
            if (registerMappingUsage.sameRegisterMapping(registerMapping)) {
                /* Todo: Legacy code validated that there were no Channels that used the mapping
                 * by calling ChannelFactory#hasChannelsForLoadProfileTypeAndMapping(this, registerMapping).
                 * This will now have to be dealt with via events. */
                this.eventService.postEvent(EventType.REGISTERMAPPING_LOADPROFILETYPE_VALIDATEDELETE.topic(), registerMappingUsage);
                iterator.remove();
            }
        }
    }

    @Override
    protected void validateDelete() {
        this.eventService.postEvent(EventType.LOADPROFILETYPE_VALIDATEDELETE.topic(), this);
    }

}