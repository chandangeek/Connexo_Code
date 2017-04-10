/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.energyict.mdc.device.data.SymmetricKeyAccessor;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.google.inject.Inject;

import java.util.Optional;

/**
 * Created by bvn on 2/28/17.
 */
public class SymmetricKeyAccessorImpl extends AbstractKeyAccessorImpl<SymmetricKeyWrapper> implements SymmetricKeyAccessor {
    private final DataModel dataModel;
    private final PkiService pkiService;
    private final Thesaurus thesaurus;

    private RefAny actualSymmetricKeyWrapperReference;
    private RefAny tempSymmetricKeyWrapperReference;

    @Inject
    public SymmetricKeyAccessorImpl(DataModel dataModel, PkiService pkiService, Thesaurus thesaurus) {
        super(pkiService);
        this.dataModel = dataModel;
        this.pkiService = pkiService;
        this.thesaurus = thesaurus;
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
    public void renew() {
        if (tempSymmetricKeyWrapperReference.isPresent()) {
            clearTempValue();
        }
        doRenewValue();
    }

    private void doRenewValue() {
        SymmetricKeyWrapper symmetricKeyWrapper = pkiService.newSymmetricKeyWrapper(getKeyAccessorType());
        symmetricKeyWrapper.generateValue(getKeyAccessorType());
        tempSymmetricKeyWrapperReference = dataModel.asRefAny(symmetricKeyWrapper);
        this.save();
    }

    @Override
    public void clearTempValue() {
        if (tempSymmetricKeyWrapperReference.isPresent()) {
            SymmetricKeyWrapper symmetricKeyWrapper = (SymmetricKeyWrapper) this.tempSymmetricKeyWrapperReference.get();
            this.tempSymmetricKeyWrapperReference = null;
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
        this.save();
    }

    @Override
    public void save() {
        Save.UPDATE.save(dataModel, this);
    }
}
