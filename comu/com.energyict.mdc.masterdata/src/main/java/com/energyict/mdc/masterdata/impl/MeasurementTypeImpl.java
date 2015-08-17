package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

/**
 * Copyrights EnergyICT
 * Date: 7/15/14
 * Time: 9:56 AM
 */
@UniqueReadingType(groups = { Save.Create.class, Save.Update.class })
public abstract class MeasurementTypeImpl extends PersistentIdObject<MeasurementType> implements MeasurementType, PersistenceAware {

    protected static final String REGISTER_DISCRIMINATOR = "0";
    public static final String CHANNEL_DISCRIMINATOR = "1";

    static final Map<String, Class<? extends MeasurementType>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends MeasurementType>>of(
                    REGISTER_DISCRIMINATOR, RegisterTypeImpl.class,
                    CHANNEL_DISCRIMINATOR, ChannelTypeImpl.class);

    enum Fields {
        READING_TYPE("readingType"),
        OBIS_CODE("obisCode"),
        INTERVAL("interval"),
        TEMPLATE_REGISTER_ID("templateRegisterId");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    protected final MasterDataService masterDataService;

    private ObisCode obisCodeCached;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_TYPE_OBIS_CODE_IS_REQUIRED + "}")
    private String obisCode;
    private String oldObisCode;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_TYPE_READING_TYPE_IS_REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();
    private boolean cumulative;
    @Size(max= Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;

    protected MeasurementTypeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, MasterDataService masterDataService) {
        super(MeasurementType.class, dataModel, eventService, thesaurus);
        this.masterDataService = masterDataService;
    }

    @Override
    public void postLoad() {
        this.synchronizeOldValues();
    }

    private void synchronizeOldValues() {
        this.oldObisCode = this.obisCode;
    }

    @Override
    public void save () {
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
        return this.readingType.getOptional()
                .flatMap(rt -> {
                    Unit unit = Unit.get(rt.getMultiplier().getSymbol() + rt.getUnit().getSymbol());
                    if (unit == null) {
                        unit = Unit.get(rt.getMultiplier().getSymbol() + rt.getUnit().getUnit().getAsciiSymbol());
                    }
                    return Optional.ofNullable(unit);
                }).orElse(Unit.getUndefined());
    }

    public Instant getModificationDate() {
        return this.getModTime();
    }

    @Override
    public int getTimeOfUse() {
        return this.readingType.isPresent() ? this.readingType.get().getTou() : 0;
    }
}
