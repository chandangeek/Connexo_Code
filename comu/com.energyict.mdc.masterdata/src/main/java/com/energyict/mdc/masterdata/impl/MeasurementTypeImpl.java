package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.Checks.is;

/**
 * Copyrights EnergyICT
 * Date: 7/15/14
 * Time: 9:56 AM
 */
@UniqueReadingType(groups = { Save.Create.class, Save.Update.class })
public abstract class MeasurementTypeImpl extends PersistentNamedObject<MeasurementType> implements MeasurementType, PersistenceAware {

    protected static final String REGISTER_DISCRIMINATOR = "0";
    protected static final String CHANNEL_DISCRIMINATOR = "1";

    static final Map<String, Class<? extends MeasurementType>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends MeasurementType>>of(
                    REGISTER_DISCRIMINATOR, RegisterTypeImpl.class,
                    CHANNEL_DISCRIMINATOR, ChannelTypeImpl.class);

    protected final MasterDataService masterDataService;
    protected final Clock clock;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @Size(max= StringColumnLengthConstraints.MEASUREMENT_TYPE_NAME, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    private ObisCode obisCodeCached;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_TYPE_OBIS_CODE_IS_REQUIRED + "}")
    private String obisCode;
    private String oldObisCode;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_TYPE_UNIT_IS_REQUIRED + "}")
    private Reference<Phenomenon> phenomenon = ValueReference.absent();
    private long oldPhenomenon;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_TYPE_READING_TYPE_IS_REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();
    private boolean cumulative;
    private String description;
    private Date modificationDate;
    @Min(value=0, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_TYPE_TIMEOFUSE_TOO_SMALL + "}")
    private int timeOfUse;

    protected MeasurementTypeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, MasterDataService masterDataService) {
        super(MeasurementType.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.masterDataService = masterDataService;
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
        this.modificationDate = this.clock.now();
        super.save();
        this.synchronizeOldValues();
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.MEASUREMENTTYPE;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.MEASUREMENTTYPE;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.MEASUREMENTTYPE;
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
        List<MeasurementType> measurementTypes = this.dataModel.mapper(MeasurementType.class).find(Fields.TEMPLATE_REGISTER_ID.fieldName(), getId());
        for (MeasurementType measurementType : measurementTypes) {
            measurementType.delete();
        }
        this.eventService.postEvent(EventType.MEASUREMENTTYPE_VALIDATEDELETE.topic(), this);
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

    enum Fields {
        READING_TYPE("readingType"),
        OBIS_CODE("obisCode"),
        PHENOMENON("phenomenon"),
        UNIT("phenomenon." + PhenomenonImpl.Fields.UNIT.fieldName()),
        TIME_OF_USE("timeOfUse"),
        INTERVAl("interval"),
        TEMPLATE_REGISTER_ID("templateRegisterId");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }
}
