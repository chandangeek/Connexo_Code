package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeChannelTypeUsage;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.exceptions.RegisterTypeAlreadyInLoadProfileTypeException;
import com.energyict.mdc.masterdata.exceptions.UnsupportedIntervalException;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.google.common.base.Optional;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @Size(max= StringColumnLengthConstraints.LOAD_PROFILE_TYPE_NAME, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            name = name.trim();
        }
        this.name = name;
    }

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
    @Size(max= StringColumnLengthConstraints.LOAD_PROFILE_TYPE_DESCRIPTION, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;
    private Date modificationDate;
    private List<LoadProfileTypeChannelTypeUsageImpl> channelTypeUsages = new ArrayList<>();

    private Clock clock;
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;

    @Inject
    public LoadProfileTypeImpl(DataModel dataModel, EventService eventService, MasterDataService masterDataService, Thesaurus thesaurus, Clock clock, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        super(LoadProfileType.class, dataModel, eventService, thesaurus);
        this.masterDataService = masterDataService;
        this.clock = clock;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
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
        this.channelTypeUsages.clear();
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

    public List<ChannelType> getChannelTypes() {
        List<ChannelType> channelTypes = new ArrayList<>(this.channelTypeUsages.size());
        for (LoadProfileTypeChannelTypeUsage channelTypeUsage : this.channelTypeUsages) {
            channelTypes.add(channelTypeUsage.getChannelType());
        }
        return channelTypes;
    }

    @Override
    public ChannelType createChannelTypeForRegisterType(RegisterType measurementTypeWithoutInterval) {
        ChannelType channelType = findOrCreateCorrespondingChannelType(measurementTypeWithoutInterval);
        for (LoadProfileTypeChannelTypeUsageImpl channelTypeUsage : this.channelTypeUsages) {
            if (channelTypeUsage.sameChannelType(channelType)) {
                throw new RegisterTypeAlreadyInLoadProfileTypeException(this.getThesaurus(), this, channelType);
            }
        }
        this.channelTypeUsages.add(new LoadProfileTypeChannelTypeUsageImpl(this, channelType));
        return channelType;
    }

    @Override
    public void removeChannelType(ChannelType channelType) {
        Iterator<LoadProfileTypeChannelTypeUsageImpl> iterator = this.channelTypeUsages.iterator();
        while (iterator.hasNext()) {
            LoadProfileTypeChannelTypeUsageImpl channelTypeUsage = iterator.next();
            if (channelTypeUsage.sameChannelType(channelType)) {
                /* Todo: Legacy code validated that there were no Channels that used the mapping
                 * by calling ChannelFactory#hasChannelsForLoadProfileTypeAndMapping(this, channelType).
                 * This will now have to be dealt with via events. */
                this.eventService.postEvent(EventType.CHANNEL_TYPE_LOADPROFILETYPE_VALIDATEDELETE.topic(), channelTypeUsage);
                iterator.remove();
            }
        }
    }

    @Override
    protected void validateDelete() {
        this.eventService.postEvent(EventType.LOADPROFILETYPE_VALIDATEDELETE.topic(), this);
    }

    private ChannelType findOrCreateCorrespondingChannelType(RegisterType measurementTypeWithoutInterval){

        Optional<ChannelType> channelType = this.masterDataService.findChannelTypeByTemplateRegisterAndInterval(measurementTypeWithoutInterval, getInterval());
        if(!channelType.isPresent()){
            ReadingType intervalAppliedReadingType = this.mdcReadingTypeUtilService.getIntervalAppliedReadingType(measurementTypeWithoutInterval.getReadingType(), getInterval(), measurementTypeWithoutInterval.getObisCode());
            ChannelType newChannelType = masterDataService.newChannelType(measurementTypeWithoutInterval, getInterval(), intervalAppliedReadingType);
            newChannelType.save();
            return newChannelType;
        } else {
            return channelType.get();
        }
    }
}