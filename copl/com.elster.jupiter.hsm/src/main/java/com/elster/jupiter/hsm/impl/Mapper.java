package com.elster.jupiter.hsm.impl;


import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;

public class Mapper {
    public static PaddingAlgorithm map(com.elster.jupiter.hsm.model.PaddingAlgorithm chainingMode) {
        /**
         * TODO: real impl
         */
        return PaddingAlgorithm.ANSI_X9_23;
    }
    public static ChainingMode map(com.elster.jupiter.hsm.model.ChainingMode chainingMode) {
        /**
         * TODO: real impl
         */
        return ChainingMode.CBC;
    }

}
