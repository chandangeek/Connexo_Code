package com.elster.jupiter.hsm.impl.config;

import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;

public abstract class HsmAbstractConfiguration implements HsmConfiguration {

    protected final PaddingAlgorithm DEFAULT_SYMMETRIC_PADDING = PaddingAlgorithm.EME_PKCS1_V1_5;
    protected final ChainingMode DEFAULT_SYMMETRIC_CHAINING = ChainingMode.CBC;


    @Override
    public ChainingMode getChainingMode(String label){
        try {
            return get(label).getChainingMode();
        } catch (HsmBaseException e) {
            return DEFAULT_SYMMETRIC_CHAINING;
        }
    }

    @Override
    public PaddingAlgorithm getPaddingAlgorithm(String label) {
        try {
            return get(label).getPaddingAlgorithm();
        } catch (HsmBaseException e) {
            return DEFAULT_SYMMETRIC_PADDING;
        }
    }

}
