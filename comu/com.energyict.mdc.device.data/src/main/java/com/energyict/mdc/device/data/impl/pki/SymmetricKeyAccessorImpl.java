/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.energyict.mdc.device.data.KeyAccessor;

import java.util.Optional;

/**
 * Created by bvn on 2/28/17.
 */
public class SymmetricKeyAccessorImpl extends AbstractKeyAccessorImpl<SymmetricKeyWrapper> implements KeyAccessor<SymmetricKeyWrapper> {

    @Override
    public SymmetricKeyWrapper getActualValue() {
        return null;
    }

    @Override
    public void setActualValue(SymmetricKeyWrapper newWrapperValue) {

    }

    @Override
    public Optional<SymmetricKeyWrapper> getTempValue() {
        return null;
    }

    @Override
    public void save() {

    }
}
