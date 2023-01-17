/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.energyict.mdc.device.data.SymmetricKeyAccessor;
import com.energyict.mdc.device.data.impl.MessageSeeds;


import java.util.Optional;

/**
 * Created by bvn on 2/28/17.
 */
public abstract class SymmetricKeyAccessorImpl extends AbstractDeviceSecurityAccessorImpl<SymmetricKeyWrapper> implements SymmetricKeyAccessor {

    DataModel dataModel;
    SecurityManagementService securityManagementService;
    Thesaurus thesaurus;

    private RefAny actualSymmetricKeyWrapperReference;
    RefAny tempSymmetricKeyWrapperReference;

    SymmetricKeyAccessorImpl() {
        super();
    }

    SymmetricKeyAccessorImpl(DataModel dataModel, SecurityManagementService securityManagementService, Thesaurus thesaurus) {
        super(securityManagementService);
        this.dataModel = dataModel;
        this.securityManagementService = securityManagementService;
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<SymmetricKeyWrapper> getActualValue() {
        if (actualSymmetricKeyWrapperReference==null) {
            return Optional.empty();
        }
        return (Optional<SymmetricKeyWrapper>) actualSymmetricKeyWrapperReference.getOptional();
    }

    @Override
    public void setActualValue(SymmetricKeyWrapper newValueWrapper) {
        actualSymmetricKeyWrapperReference = dataModel.asRefAny(newValueWrapper);
    }

    @Override
    public Optional<SymmetricKeyWrapper> getTempValue() {
        if (tempSymmetricKeyWrapperReference==null) {
            return Optional.empty();
        }
        return (Optional<SymmetricKeyWrapper>) tempSymmetricKeyWrapperReference.getOptional();
    }

    @Override
    public void setTempValue(SymmetricKeyWrapper newValueWrapper) {
        tempSymmetricKeyWrapperReference = dataModel.asRefAny(newValueWrapper);
    }


    @Override
    public void clearTempValue() {
        if (getTempValue().isPresent()) {
            super.clearTempValue();
            SymmetricKeyWrapper symmetricKeyWrapper = (SymmetricKeyWrapper) this.tempSymmetricKeyWrapperReference.get();
            this.tempSymmetricKeyWrapperReference = null;
            symmetricKeyWrapper.delete();
            this.save();
        }
    }

    @Override
    public void clearActualValue() {
        if (getActualValue().isPresent()) {
            SymmetricKeyWrapper symmetricKeyWrapper = (SymmetricKeyWrapper) this.actualSymmetricKeyWrapperReference.get();
            this.actualSymmetricKeyWrapperReference = null;
            symmetricKeyWrapper.delete();
            this.save();
        }
    }

    @Override
    public void swapValues() {
        if (!actualSymmetricKeyWrapperReference.isPresent()) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ACTUAL_VALUE_NOT_SET);
        }
        if (!tempSymmetricKeyWrapperReference.isPresent()) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.TEMP_VALUE_NOT_SET);
        }
        Object actual = actualSymmetricKeyWrapperReference.get();
        actualSymmetricKeyWrapperReference = dataModel.asRefAny(tempSymmetricKeyWrapperReference.get());
        tempSymmetricKeyWrapperReference = dataModel.asRefAny(actual);
        super.swapValues();
        this.save();
    }

    @Override
    public void delete() {
        if (actualSymmetricKeyWrapperReference!=null && actualSymmetricKeyWrapperReference.isPresent()) {
            ((SymmetricKeyWrapper)actualSymmetricKeyWrapperReference.get()).delete();
        }
        if (tempSymmetricKeyWrapperReference!=null && tempSymmetricKeyWrapperReference.isPresent()) {
            ((SymmetricKeyWrapper)tempSymmetricKeyWrapperReference.get()).delete();
        }
        getDevice().removeSecurityAccessor(this);
    }

    @Override
    public void save() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void touch(){
        this.dataModel.touch(this);
    }
}
