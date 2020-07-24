/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ValidObisCode;
import com.energyict.mdc.common.interval.Temporals;
import com.energyict.mdc.common.masterdata.ChannelType;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.masterdata.LoadProfileTypeChannelTypeUsage;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.exceptions.RegisterTypeAlreadyInLoadProfileTypeException;
import com.energyict.mdc.masterdata.exceptions.RegisterTypesNotMappableToLoadProfileTypeIntervalException;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.energyict.obis.ObisCode;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link LoadProfileType} interface.
 * <p>
 *
 * Date: 11-jan-2011
 * Time: 16:05:54
 */
public class LoadProfileTypeImpl extends PersistentNamedObject<LoadProfileType> implements LoadProfileType, PersistenceAware {

    private final MasterDataService masterDataService;
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String name;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String obisCode;
    private String oldObisCode;
    @ValidObisCode(groups = { Save.Create.class, Save.Update.class })
    private ObisCode obisCodeCached;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @IncorrectTimeDuration(groups = {Save.Create.class, Save.Update.class})
    private TimeDuration interval;
    private long oldIntervalSeconds;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String description;
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.AT_LEAST_ONE_REGISTER_TYPE_REQUIRED + "}")
    private List<LoadProfileTypeChannelTypeUsageImpl> registerTypes = new ArrayList<>();
    private boolean intervalChanged = false;

    @Inject
    public LoadProfileTypeImpl(DataModel dataModel, EventService eventService, MasterDataService masterDataService, Thesaurus thesaurus, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        super(LoadProfileType.class, dataModel, eventService, thesaurus);
        this.masterDataService = masterDataService;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    static LoadProfileTypeImpl from(DataModel dataModel, String name, ObisCode obisCode, TimeDuration interval, Collection<RegisterType> registerTypes) {
        LoadProfileTypeImpl loadProfileType = dataModel.getInstance(LoadProfileTypeImpl.class);
        Collection<RegisterType> failedRegisterTypes = loadProfileType.initialize(name, obisCode, interval, registerTypes);
        if (failedRegisterTypes.isEmpty()) {
            return loadProfileType;
        } else {
            throw new RegisterTypesNotMappableToLoadProfileTypeIntervalException(loadProfileType.thesaurus, loadProfileType, failedRegisterTypes);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            name = name.trim();
        }
        this.name = name;
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

    @Override
    public void postLoad() {
        this.synchronizeOldValues();
    }

    @Override
    public void save() {
        if(obisCodeCached == null && obisCode != null){
            this.getObisCode();
        }
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
        } else {
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
        } else {
            this.obisCode = obisCode.toString();
            this.obisCodeCached = obisCode;
        }
    }

    // Used by EventType
    @SuppressWarnings("unused")
    public String getOldObisCode() {
        return oldObisCode;
    }

    @Override
    public TemporalAmount interval() {
        return Temporals.toTemporalAmount(interval);
    }

    @Override
    public void setInterval(TimeDuration interval) {
        this.intervalChanged = this.interval != null && !this.interval.equals(interval) && !this.registerTypes.isEmpty();
        this.interval = interval;
    }

    private void updateChannelTypeUsagesAccordingToNewInterval() {
        List<RegisterType> templateRegisters =
                this.registerTypes
                        .stream()
                        .map(LoadProfileTypeChannelTypeUsage::getChannelType)
                        .map(ChannelType::getTemplateRegister)
                        .collect(Collectors.toList());
        registerTypes.clear();
        templateRegisters.forEach(this::createChannelTypeForRegisterType);
    }

    // Used by EventType
    @SuppressWarnings("unused")
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
        return this.registerTypes
                .stream()
                .map(LoadProfileTypeChannelTypeUsage::getChannelType)
                .collect(Collectors.toList());
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
    public Optional<ChannelType> findChannelType(RegisterType measurementTypeWithoutInterval) {
        return this.masterDataService.findChannelTypeByTemplateRegisterAndInterval(measurementTypeWithoutInterval, Temporals.toTimeDuration(interval()));
    }

    private Optional<ChannelType> findOrCreateCorrespondingChannelType(RegisterType measurementTypeWithoutInterval) {
        Optional<ChannelType> channelType = this.masterDataService.findChannelTypeByTemplateRegisterAndInterval(measurementTypeWithoutInterval, Temporals.toTimeDuration(interval()));
        if (!channelType.isPresent()) {
            return Optional.of(this.newChannelType(measurementTypeWithoutInterval, this.mdcReadingTypeUtilService.getOrCreateIntervalAppliedReadingType(
                    measurementTypeWithoutInterval.getReadingType(),
                    Optional.ofNullable(Temporals.toTimeDuration(interval())),
                    measurementTypeWithoutInterval.getObisCode()
            )));
        } else {
            return channelType;
        }
    }

    private ChannelType newChannelType(RegisterType measurementTypeWithoutInterval, ReadingType intervalAppliedReadingType) {
        ChannelType newChannelType = this.masterDataService.newChannelType(measurementTypeWithoutInterval, Temporals.toTimeDuration(interval()), intervalAppliedReadingType);
        newChannelType.save();
        return newChannelType;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

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
}