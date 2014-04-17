package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.MasterDataService;

import javax.inject.Inject;
import java.util.Date;

import static com.elster.jupiter.util.Checks.is;

/**
 * @author Karel
 */

public class PhenomenonImpl extends PersistentNamedObject<Phenomenon> implements Phenomenon {

    enum Fields {
        NAME("name"),
        DESCRIPTION("description"),
        UNIT("unitString"),
        MEASUREMENT_CODE("measurementCode"),
        EDI_CODE("ediCode"),
        MODIFICATION_DATE("modificationDate");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private final MasterDataService masterDataService;

    private String unitString;
    private Unit unit;
    private String description;
    private String measurementCode;
    private String ediCode;
    private Date modificationDate;

    @Inject
    protected PhenomenonImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, MasterDataService masterDataService) {
        super(Phenomenon.class, dataModel, eventService, thesaurus);
        this.masterDataService = masterDataService;
    }

    Phenomenon initialize(String name, Unit unit) {
        setName(name);
        setUnit(unit);
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public String getEdiCode() {
        return this.ediCode;
    }

    public String getMeasurementCode() {
        return this.measurementCode;
    }

    public Unit getUnit() {
        if (this.unit == null && !is(this.unitString).empty()) {
            this.unit = Unit.fromDb(this.unitString);
        }
        return this.unit;
    }

    @Override
    public String toString() {
        String unitString = getUnit().toString();
        if (unitString.isEmpty()) {
            return getName();
        } else {
            return getName() + " (" + unitString + ")";
        }
    }

    public boolean isUndefined() {
        return this.getUnit().equals(Unit.getUndefined());
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.PHENOMENON;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.PHENOMENON;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PHENOMENON;
    }

    @Override
    protected void validateDelete() {
        //TODO what to do with the Undefined? Who puts it in here?
//        if (isUndefined()) {
//            throw new BusinessException("cannotDeletePhenomenonX",
//                    "The phenomenon '{0}' cannot be deleted", getName());
//        }
        this.eventService.postEvent(EventType.PHENOMENON_VALIDATEDELETE.topic(), this);
    }

    public boolean isUnitless() {
        return getUnit().isUndefined();
    }

    @Override
    public void setUnit(Unit unit) {
        if(unit != null){
            this.unitString = unit.dbString();
        }
        this.unit = unit;
    }

    @Override
    public void setMeasurementCode(String measurementCode) {
        this.measurementCode = measurementCode;
    }

    @Override
    public void setEdiCode(String ediCode) {
        this.ediCode = ediCode;
    }
}
