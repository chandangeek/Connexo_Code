/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.references.RefAnyImpl;
import com.elster.jupiter.pki.PassphraseWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.wrappers.symmetric.PlaintextPassphraseImpl;
import com.energyict.mdc.device.data.PassphraseAccessor;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.google.inject.Inject;

import javax.xml.bind.annotation.XmlElement;
import java.util.Optional;

/**
 * Created by bvn on 2/28/17.
 */
public class PassphraseAccessorImpl extends AbstractDeviceSecurityAccessorImpl<PassphraseWrapper> implements PassphraseAccessor {
    private DataModel dataModel;
    private SecurityManagementService securityManagementService;
    private Thesaurus thesaurus;

    private RefAny actualPassphraseWrapperReference;
    private RefAny tempPassphraseWrapperReference;

    public PassphraseAccessorImpl() {
        super();
    }

    @Inject
    public PassphraseAccessorImpl(DataModel dataModel, SecurityManagementService securityManagementService, Thesaurus thesaurus) {
        super(securityManagementService);
        this.dataModel = dataModel;
        this.securityManagementService = securityManagementService;
        this.thesaurus = thesaurus;
    }

    @Override
    @XmlElement(type = PlaintextPassphraseImpl.class)
    public Optional<PassphraseWrapper> getActualValue() {
        if (actualPassphraseWrapperReference==null) {
            return Optional.empty();
        }
        return (Optional<PassphraseWrapper>) actualPassphraseWrapperReference.getOptional();
    }

    @Override
    public void setActualValue(PassphraseWrapper newValueWrapper) {
        if (dataModel != null) {
            actualPassphraseWrapperReference = dataModel.asRefAny(newValueWrapper);
        } else {
            actualPassphraseWrapperReference = new RefAnyImpl(null, null);
            ((RefAnyImpl) actualPassphraseWrapperReference).setOptional(Optional.ofNullable(newValueWrapper));
        }
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
        PassphraseWrapper passphraseWrapper = securityManagementService.newPassphraseWrapper(getSecurityAccessorType());
        passphraseWrapper.generateValue();
        tempPassphraseWrapperReference = dataModel.asRefAny(passphraseWrapper);
        this.save();
    }

    @Override
    public void clearTempValue() {
        if (getTempValue().isPresent()) {
            super.clearTempValue();
            PassphraseWrapper passphraseWrapper = (PassphraseWrapper) this.tempPassphraseWrapperReference.get();
            this.tempPassphraseWrapperReference = null;
            passphraseWrapper.delete();
            this.save();
        }
    }

    @Override
    public void clearActualValue() {
        if (getActualValue().isPresent()) {
            PassphraseWrapper passphraseWrapper = (PassphraseWrapper) this.actualPassphraseWrapperReference.get();
            this.actualPassphraseWrapperReference = null;
            passphraseWrapper.delete();
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
