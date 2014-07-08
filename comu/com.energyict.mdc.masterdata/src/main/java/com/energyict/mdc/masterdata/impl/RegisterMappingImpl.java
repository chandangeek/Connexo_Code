package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeRegisterMappingUsage;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.conditions.Where.where;

@UniqueReadingType(groups = { Save.Create.class, Save.Update.class })
public class RegisterMappingImpl extends PersistentNamedObject<RegisterMapping> implements RegisterMapping, PersistenceAware {

    enum Fields {
        READING_TYPE("readingType"),
        OBIS_CODE("obisCode"),
        PHENOMENON("phenomenon"),
        UNIT("phenomenon." + PhenomenonImpl.Fields.UNIT.fieldName()),
        TIME_OF_USE("timeOfUse");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private final MasterDataService masterDataService;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @Size(max= StringColumnLengthConstraints.REGISTER_MAPPING_NAME, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;

    private ObisCode obisCodeCached;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED + "}")
    private String obisCode;
    private String oldObisCode;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_MAPPING_UNIT_IS_REQUIRED + "}")
    private Reference<Phenomenon> phenomenon = ValueReference.absent();
    private long oldPhenomenon;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_MAPPING_READING_TYPE_IS_REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();
    private boolean cumulative;
    private String description;
    private Date modificationDate;
    @Min(value=0, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_MAPPING_TIMEOFUSE_TOO_SMALL + "}")
    private int timeOfUse;

    private Clock clock;

    @Inject
    public RegisterMappingImpl(DataModel dataModel, EventService eventService, MasterDataService masterDataService, Thesaurus thesaurus, Clock clock) {
        super(RegisterMapping.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.masterDataService = masterDataService;
    }

    RegisterMappingImpl initialize(String name, ObisCode obisCode, Phenomenon phenomenon, ReadingType readingType, int timeOfUse) {
        this.setName(name);
        this.setObisCode(obisCode);
        this.setPhenomenon(phenomenon);
        this.setReadingType(readingType);
        this.setTimeOfUse(timeOfUse);
        return this;
    }

    @Override
    public void postLoad() {
        this.synchronizeOldValues();
    }

    private void synchronizeOldValues() {
        this.oldObisCode = this.obisCode;
        this.oldPhenomenon = this.phenomenon.get().getId();
    }

    @Override
    public void save () {
        validateUniqueObisCodeAndPhenomenonAndTimeOfUse();
        this.modificationDate = this.clock.now();
        super.save();
        this.synchronizeOldValues();
    }

    private void validateUniqueObisCodeAndPhenomenonAndTimeOfUse() {
        if (this.phenomenon.isPresent() && this.obisCodeCached != null) {
            RegisterMapping otherRegisterMapping = this.findOtherByObisCodeAndPhenomenonAndTimeOfUse();
            if (otherRegisterMapping != null) {
                throw DuplicateObisCodeException.forRegisterMapping(
                        this.getThesaurus(),
                        obisCodeCached,
                        phenomenon.get(),
                        timeOfUse,
                        otherRegisterMapping);
            }
        }
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.REGISTERMAPPING;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.REGISTERMAPPING;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.REGISTERMAPPING;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (name != null) {
            name = name.trim();
        }
        this.name = name;
    }

    @Override
    public ObisCode getObisCode() {
        if (this.obisCodeCached == null && !is(this.obisCode).empty()) {
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

    // Used by the update event
    public String getOldObisCode() {
        return oldObisCode;
    }

    private RegisterMapping findOtherByObisCodeAndPhenomenonAndTimeOfUse() {
        Condition condition =
                where(Fields.OBIS_CODE.fieldName()).isEqualTo(this.getObisCode().toString()).
            and(where("phenomenon").isEqualTo(this.getPhenomenon()).
            and(where("timeOfUse").isEqualTo(this.getTimeOfUse())));
        List<RegisterMapping> registerMappings = this.getDataMapper().select(condition);
        if (!registerMappings.isEmpty()) {
            for (RegisterMapping registerMapping : registerMappings) {
                if (registerMapping.getId() != this.getId()) {
                    // The RegisterMapping that was found is NOT the one we are updating so we have found another one
                    return registerMapping;
                }
            }
            // No other RegisterMapping found
            return null;
        }
        else {
            // No other RegisterMapping found or this RegisterMapping is not peristent yet or the other is really different because the id does not match
            return null;
        }
    }

    @Override
    public List<RegisterGroup> getRegisterGroups() {
        Map<Long, RegisterGroup> groups = new HashMap<>();
        List<RegisterMappingInGroup> registerMappingInGroups = this.dataModel.mapper(RegisterMappingInGroup.class).find("registerMapping", this);
        for (RegisterMappingInGroup registerMappingInGroup : registerMappingInGroups) {
            RegisterGroup group = registerMappingInGroup.getRegisterGroup();
            if (!groups.containsKey(group.getId())) {
                groups.put(group.getId(), group);
            }
        }
        return new ArrayList<>(groups.values());
    }

    private boolean phenomenonChanged(Phenomenon phenomenon) {
        return ((!this.phenomenon.isPresent() && phenomenon != null)
            || (phenomenon != null && (this.getPhenomenon().getId() != phenomenon.getId())));
    }

    @Override
    public Phenomenon getPhenomenon() {
        return phenomenon.get();
    }

    public void setPhenomenon(Phenomenon phenomenon) {
        if (phenomenon == null) {
            this.phenomenon.setNull();
        }
        else {
            this.phenomenon.set(phenomenon);
        }
    }

    public long getOldPhenomenon() {
        return oldPhenomenon;
    }

    public void setReadingType(ReadingType readingType) {
        this.readingType.set(readingType);
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType.orNull();
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    protected void validateDelete() {
        this.validateNotUsedByLoadProfileTypes();
        this.eventService.postEvent(EventType.REGISTERMAPPING_VALIDATEDELETE.topic(), this);
    }

    private void validateNotUsedByLoadProfileTypes() {
        List<LoadProfileTypeRegisterMappingUsage> loadProfileTypeUsages = this.dataModel.mapper(LoadProfileTypeRegisterMappingUsage.class).find("registerMapping", this);
        if (!loadProfileTypeUsages.isEmpty()) {
            Set<LoadProfileType> loadProfileTypes = new HashSet<>();
            for (LoadProfileTypeRegisterMappingUsage loadProfileTypeUsage : loadProfileTypeUsages) {
                loadProfileTypes.add(loadProfileTypeUsage.getLoadProfileType());
            }
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByLoadprofileTypes(this.getThesaurus(), this, new ArrayList<>(loadProfileTypes));
        }
    }

    @Override
    public boolean isCumulative() {
        return this.cumulative;
    }

    @Override
    public void setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
    }

    @Override
    public Unit getUnit() {
        return this.phenomenon.get().getUnit();
    }

    @Override
    public void setUnit(Unit unit) {
        Optional<Phenomenon> phenomenon = this.masterDataService.findPhenomenonByUnit(unit);
        setPhenomenon(phenomenon.orNull());
    }

    public Date getModificationDate() {
        return this.modificationDate;
    }

    @Override
    public int getTimeOfUse() {
        return timeOfUse;
    }

    @Override
    public void setTimeOfUse(int timeOfUse) {
        this.timeOfUse = timeOfUse;
    }

}