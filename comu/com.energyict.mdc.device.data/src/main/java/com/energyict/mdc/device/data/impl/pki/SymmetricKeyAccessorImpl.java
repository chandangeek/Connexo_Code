/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.energyict.mdc.device.data.KeyAccessor;

import com.google.inject.Inject;

import java.util.Optional;

/**
 * Created by bvn on 2/28/17.
 */
public class SymmetricKeyAccessorImpl extends AbstractKeyAccessorImpl<SymmetricKeyWrapper> implements KeyAccessor<SymmetricKeyWrapper> {
    private final DataModel dataModel;

    private RefAny actualSymmetricKeyWrapperReference;
    private RefAny tempSymmetricKeyWrapperReference;

    @Inject
    public SymmetricKeyAccessorImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public SymmetricKeyWrapper getActualValue() {
        return (SymmetricKeyWrapper) actualSymmetricKeyWrapperReference.get();
    }

    @Override
    public void setActualValue(SymmetricKeyWrapper newWrapperValue) {
        actualSymmetricKeyWrapperReference = dataModel.asRefAny(newWrapperValue);
    }

    @Override
    public Optional<SymmetricKeyWrapper> getTempValue() {
        return (Optional<SymmetricKeyWrapper>) tempSymmetricKeyWrapperReference.getOptional();
    }

    @Override
    public void save() {
        Save.UPDATE.save(dataModel, this);
    }
}
