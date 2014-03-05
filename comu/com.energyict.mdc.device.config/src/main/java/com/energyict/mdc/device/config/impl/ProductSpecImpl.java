package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteDefaultProductSpecException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Provides an implementation for the {@link ProductSpec} interace.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since Jan 29, 2014 (16:31)
 */
@UniqueReadingType(groups = {Save.Create.class, Save.Update.class})
public class ProductSpecImpl implements ProductSpec {

    private long id;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.READING_TYPE_IS_REQUIRED_KEY + "}")
    private final Reference<ReadingType> readingType = ValueReference.absent();
    private Date modificationDate;

    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;

    @Inject
    public ProductSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    static ProductSpecImpl from (DataModel dataModel, ReadingType readingType) {
        return dataModel.getInstance(ProductSpecImpl.class).initialize(readingType);
    }

    ProductSpecImpl initialize (ReadingType readingType) {
        this.setReadingType(readingType);
        return this;
    }

    private DataMapper<ProductSpec> getDataMapper() {
        return this.dataModel.mapper(ProductSpec.class);
    }

    @Override
    public void save () {
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
        this.getDataMapper().update(this);
    }


    public void delete() {
        this.validateDelete();
        this.notifyDependents();
        this.getDataMapper().remove(this);
    }

    private void validateDelete() {
        if (this.id == 0) {
            throw new CannotDeleteDefaultProductSpecException(this.thesaurus);
        }
        List<RegisterMapping> registerMappings = this.dataModel.mapper(RegisterMapping.class).find("productSpec", this);
        if (!registerMappings.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.productSpecIsStillInUse(this.thesaurus, this, registerMappings);
        }
    }

    private void notifyDependents() {
        this.eventService.postEvent(EventType.DEVICETYPE_DELETED.topic(), this);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Unit getUnit() {
        return mdcReadingTypeUtilService.getReadingTypeInformationFor(readingType.get()).getUnit();
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType.get();
    }

    @Override
    public void setReadingType(ReadingType readingType) {
        this.readingType.set(readingType);
    }

    @Override
    public String getDescription() {
        return this.getReadingType().getName();
    }

}