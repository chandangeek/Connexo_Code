package com.elster.jupiter.validation.impl;


import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.validation.MeterValidation;
import com.google.inject.Inject;

public class MeterValidationImpl implements MeterValidation {

    private boolean isActive = false;
    private Reference<MeterActivation> meterActivation = ValueReference.absent();

    private transient boolean saved = true;
    private final DataModel dataModel;

    @Inject
    MeterValidationImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    MeterValidationImpl init(MeterActivation meterActivation) {
        this.meterActivation.set(meterActivation);
        return this;
    }

    static MeterValidationImpl from(DataModel dataModel, MeterActivation meterActivation) {
        MeterValidationImpl meterValidation = dataModel.getInstance(MeterValidationImpl.class);
        meterValidation.saved = false;
        return meterValidation.init(meterActivation);
    }

    @Override
    public boolean getActivationStatus() {
        return isActive;
    }

    @Override
    public void setActivationStatus(boolean status) {
        this.isActive = status;
    }

    @Override
    public void save() {
        if (!saved) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

}
