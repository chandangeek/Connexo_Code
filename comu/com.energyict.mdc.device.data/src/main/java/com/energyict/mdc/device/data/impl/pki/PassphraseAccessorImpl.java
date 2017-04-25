/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.pki.PassphraseWrapper;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.energyict.mdc.device.data.PassphraseAccessor;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.google.inject.Inject;

import java.util.Optional;

/**
 * Created by bvn on 2/28/17.
 */
public class PassphraseAccessorImpl extends AbstractKeyAccessorImpl<PassphraseWrapper> implements PassphraseAccessor {
    private final DataModel dataModel;
    private final PkiService pkiService;
    private final Thesaurus thesaurus;

    private RefAny actualPassphraseWrapperReference;
    private RefAny tempPassphraseWrapperReference;

    @Inject
    public PassphraseAccessorImpl(DataModel dataModel, PkiService pkiService, Thesaurus thesaurus) {
        super(pkiService);
        this.dataModel = dataModel;
        this.pkiService = pkiService;
        this.thesaurus = thesaurus;
    }

    @Override
    public PassphraseWrapper getActualValue() {
        return (PassphraseWrapper) actualPassphraseWrapperReference.get();
    }

    @Override
    public void setActualValue(PassphraseWrapper newWrapperValue) {
        actualPassphraseWrapperReference = dataModel.asRefAny(newWrapperValue);
    }

    @Override
    public Optional<PassphraseWrapper> getTempValue() {
        if (tempPassphraseWrapperReference==null) {
            return Optional.empty();
        }
        return (Optional<PassphraseWrapper>) tempPassphraseWrapperReference.getOptional();
    }

    @Override
    public void setTempValue(PassphraseWrapper newValueWrapper) {
        tempPassphraseWrapperReference = dataModel.asRefAny(newValueWrapper);
    }

    @Override
    public void renew() {
        if (tempPassphraseWrapperReference.isPresent()) {
            clearTempValue();
        }
        doRenewValue();
    }

    private void doRenewValue() {
        SymmetricKeyWrapper symmetricKeyWrapper = pkiService.newSymmetricKeyWrapper(getKeyAccessorType());
        symmetricKeyWrapper.generateValue(getKeyAccessorType());
        tempPassphraseWrapperReference = dataModel.asRefAny(symmetricKeyWrapper);
        this.save();
    }

    @Override
    public void clearTempValue() {
        if (tempPassphraseWrapperReference.isPresent()) {
            super.clearTempValue();
            SymmetricKeyWrapper symmetricKeyWrapper = (SymmetricKeyWrapper) this.tempPassphraseWrapperReference.get();
            this.tempPassphraseWrapperReference = null;
            symmetricKeyWrapper.delete();
            this.save();
        }
    }

    @Override
    public void swapValues() {
        if (!actualPassphraseWrapperReference.isPresent()) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ACTUAL_VALUE_NOT_SET);
        }
        if (!tempPassphraseWrapperReference.isPresent()) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.TEMP_VALUE_NOT_SET);
        }
        Object actual = actualPassphraseWrapperReference.get();
        actualPassphraseWrapperReference = dataModel.asRefAny(tempPassphraseWrapperReference.get());
        tempPassphraseWrapperReference = dataModel.asRefAny(actual);
        super.swapValues();
        this.save();
    }

    @Override
    public void delete() {
        if (actualPassphraseWrapperReference!=null && actualPassphraseWrapperReference.isPresent()) {
            ((PassphraseWrapper)actualPassphraseWrapperReference.get()).delete();
        }
        if (tempPassphraseWrapperReference!=null && tempPassphraseWrapperReference.isPresent()) {
            ((PassphraseWrapper)tempPassphraseWrapperReference.get()).delete();
        }
        getDevice().removeKeyAccessor(this);
    }

    @Override
    public void save() {
        Save.UPDATE.save(dataModel, this);
    }
}
