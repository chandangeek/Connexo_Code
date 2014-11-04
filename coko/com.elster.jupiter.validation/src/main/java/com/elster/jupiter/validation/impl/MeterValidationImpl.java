package com.elster.jupiter.validation.impl;


import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import javax.inject.Inject;

public class MeterValidationImpl {

    private boolean isActive = false;
    private Reference<Meter> meter = ValueReference.absent();

    private transient boolean saved = true;
    private final DataModel dataModel;

    @Inject
    MeterValidationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    MeterValidationImpl init(Meter meter) {
        this.meter.set(meter);
        saved = false;
        return this;
    }

    public boolean getActivationStatus() {
        return isActive;
    }

    public void setActivationStatus(boolean status) {
        this.isActive = status;
    }

    public void save() {
        if (!saved) {
            Save.CREATE.save(dataModel, this);
            saved = true;
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

}
