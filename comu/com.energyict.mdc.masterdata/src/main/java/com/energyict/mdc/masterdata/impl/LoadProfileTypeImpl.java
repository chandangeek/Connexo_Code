package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeChannelTypeUsage;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.exceptions.RegisterTypeAlreadyInLoadProfileTypeException;
import com.energyict.mdc.masterdata.exceptions.RegisterTypesNotMappableToLoadProfileTypeIntervalException;
import com.energyict.mdc.masterdata.exceptions.UnsupportedIntervalException;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Copyrights EnergyICT
 * Date: 11-jan-2011
 * Time: 16:05:54
 */
public class LoadProfileTypeImpl extends PersistentNamedObject<LoadProfileType> implements LoadProfileType, PersistenceAware {

    enum Fields {
        OBIS_CODE("obisCode"),
        REGISTER_TYPES("registerTypes");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @Size(max= Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
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

    private final MasterDataService masterDataService;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED + "}")
    private String obisCode;
    private String oldObisCode;
    private ObisCode obisCodeCached;
    private TimeDuration interval;
    private long oldIntervalSeconds;
    @Size(max= Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.AT_LEAST_ONE_REGISTER_TYPE_REQUIRED + "}")
    private List<LoadProfileTypeChannelTypeUsageImpl> registerTypes = new ArrayList<>();

    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;

    private boolean intervalChanged = false;

    @Inject
    public LoadProfileTypeImpl(DataModel dataModel, EventService eventService, MasterDataService masterDataService, Thesaurus thesaurus, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        super(LoadProfileType.class, dataModel, eventService, thesaurus);
        this.masterDataService = masterDataService;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    Collection<RegisterType> initialize(String name, ObisCode obisCode, TimeDuration interval, Collection<RegisterType> registerTypes) {
        this.setName(name);
        this.setObisCode(obisCode);
        this.setInterval(interval);
        Collection<RegisterType> failedRegisterTypes = new ArrayList<>();
        for (RegisterType registerType : registerTypes) {
            Optional<ChannelType> channelType = this.createChannelTypeForRegisterType(registerType);
            if (!channelType.isPresent()) {
                failedRegisterTypes.add(registerType);
            }
        }
        return failedRegisterTypes;
    }

    static LoadProfileTypeImpl from(DataModel dataModel, String name, ObisCode obisCode, TimeDuration interval, Collection<RegisterType> registerTypes) {
        LoadProfileTypeImpl loadProfileType = dataModel.getInstance(LoadProfileTypeImpl.class);
        Collection<RegisterType> failedRegisterTypes = loadProfileType.initialize(name, obisCode, interval, registerTypes);
        if (failedRegisterTypes.isEmpty()) {
            return loadProfileType;
        }
        else {
            throw new RegisterTypesNotMappableToLoadProfileTypeIntervalException(loadProfileType.thesaurus, loadProfileType, failedRegisterTypes);
        }
    }

    @Override
    public void postLoad() {
        this.synchronizeOldValues();
    }

    @Override
    public void save () {
        if (intervalChanged) {
            updateChannelTypeUsagesAccordingToNewInterval();
            this.intervalChanged = false;
        }
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
        this.registerTypes.clear();
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
        if ((interval.getTimeUnit() == TimeDuration.TimeUnit.WEEKS)) {
            throw UnsupportedIntervalException.weeksAreNotSupportedForLoadProfileTypes(this.getThesaurus(), this);
        }
        if (countMustBeOneFor(interval) && interval.getCount() != 1) {
            throw UnsupportedIntervalException.multipleNotSupported(this.getThesaurus(), interval);
        }
        if ((interval.getCount() <= 0)) {
            throw UnsupportedIntervalException.strictlyPositive(this.getThesaurus(), interval);
        }
        this.intervalChanged = this.interval != null && !this.interval.equals(interval) && !this.registerTypes.isEmpty();
        this.interval = interval;
    }

    private void updateChannelTypeUsagesAccordingToNewInterval() {
        List<RegisterType> templateRegisters = new ArrayList<>(registerTypes.size());
        for (LoadProfileTypeChannelTypeUsageImpl channelTypeUsage : registerTypes) {
            templateRegisters.add(channelTypeUsage.getChannelType().getTemplateRegister());
        }
        registerTypes.clear();
        for (RegisterType templateRegister : templateRegisters) {
            createChannelTypeForRegisterType(templateRegister);
        }
    }

    private boolean countMustBeOneFor(TimeDuration interval) {
        return interval.getTimeUnit() == TimeDuration.TimeUnit.DAYS || interval.getTimeUnit() == TimeDuration.TimeUnit.MONTHS || interval.getTimeUnit() == TimeDuration.TimeUnit.YEARS;
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
        List<ChannelType> channelTypes = new ArrayList<>(this.registerTypes.size());
        for (LoadProfileTypeChannelTypeUsage channelTypeUsage : this.registerTypes) {
            channelTypes.add(channelTypeUsage.getChannelType());
        }
        return channelTypes;
    }

    @Override
    public Optional<ChannelType> createChannelTypeForRegisterType(RegisterType measurementTypeWithoutInterval) {
        Optional<ChannelType> channelType = findOrCreateCorrespondingChannelType(measurementTypeWithoutInterval);
        if (channelType.isPresent()) {
            for (LoadProfileTypeChannelTypeUsageImpl channelTypeUsage : this.registerTypes) {
                if (channelTypeUsage.sameChannelType(channelType.get())) {
                    throw new RegisterTypeAlreadyInLoadProfileTypeException(this.getThesaurus(), this, channelType.get());
                }
            }
            this.registerTypes.add(new LoadProfileTypeChannelTypeUsageImpl(this, channelType.get()));
        }
        return channelType;
    }

    @Override
    public void removeChannelType(ChannelType channelType) {
        Iterator<LoadProfileTypeChannelTypeUsageImpl> iterator = this.registerTypes.iterator();
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

    @Override
    public Optional<ChannelType> findChannelType(RegisterType measurementTypeWithoutInterval){
        return this.masterDataService.findChannelTypeByTemplateRegisterAndInterval(measurementTypeWithoutInterval, getInterval());
    }

    private Optional<ChannelType> findOrCreateCorrespondingChannelType(RegisterType measurementTypeWithoutInterval) {
        Optional<ChannelType> channelType = this.masterDataService.findChannelTypeByTemplateRegisterAndInterval(measurementTypeWithoutInterval, getInterval());
        if (!channelType.isPresent()) {
            return this.mdcReadingTypeUtilService
                    .getIntervalAppliedReadingType(
                            measurementTypeWithoutInterval.getReadingType(),
                            getInterval(),
                            measurementTypeWithoutInterval.getObisCode())
                    .map(rt -> this.newChannelType(measurementTypeWithoutInterval, rt));
        } else {
            return channelType;
        }
    }

    private ChannelType newChannelType(RegisterType measurementTypeWithoutInterval, ReadingType intervalAppliedReadingType) {
        ChannelType newChannelType = this.masterDataService.newChannelType(measurementTypeWithoutInterval, getInterval(), intervalAppliedReadingType);
        newChannelType.save();
        return newChannelType;
    }

}