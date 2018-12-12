package com.elster.jupiter.hsm.model.krypto;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.key.KEKEncryptionMethod;

public class HsmAlgorithmSpecs {

    private final ChainingMode chainingMode;
    private final PaddingAlgorithm paddingAlgorithm;
    private final KEKEncryptionMethod kekEncryptionMethod;

    /**
     * As for any algorithm we need to have chaining mode and padding algorithm yet in HSM/JSS there is also KekEncryptionMethod which is actually the same but different
     * and therefore this class is error prone for the moment while there is no check if the fields in here are consistent. This can be fixed in future if we can find a mapping between algorithm, chaining mode, padding
     * and the consequent kek encryption method... I did not found it yet
     *
     * @param chainingMode
     * @param paddingAlgorithm
     * @param kekEncryptionMethod
     */
    public HsmAlgorithmSpecs(ChainingMode chainingMode, PaddingAlgorithm paddingAlgorithm, KEKEncryptionMethod kekEncryptionMethod) {
        this.chainingMode = chainingMode;
        this.paddingAlgorithm = paddingAlgorithm;
        this.kekEncryptionMethod = kekEncryptionMethod;
    }


    public PaddingAlgorithm getPaddingAlgorithm() {
        return paddingAlgorithm;
    }

    public ChainingMode getChainingMode() {
        return chainingMode;
    }


    public KEKEncryptionMethod getKekEncryptionMethod() {
        return kekEncryptionMethod;
    }
}
