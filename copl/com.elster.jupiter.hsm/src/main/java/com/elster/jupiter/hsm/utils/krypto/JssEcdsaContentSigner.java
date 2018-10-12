/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.utils.krypto;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.ECDSASignature;
import com.atos.worldline.jss.api.cardmanagement.CertificateManagement;
import com.atos.worldline.jss.api.cardmanagement.CertificateRequestFormat;
import com.atos.worldline.jss.api.key.PrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * Created by H216758 on 10/10/2018.
 */
public class JssEcdsaContentSigner extends AbstractContentSigner {

    public JssEcdsaContentSigner(PrivateKey privateKey, String digestAlgorithm) {
        super(privateKey, digestAlgorithm);
    }

    public JssEcdsaContentSigner(PrivateKey privateKey) {
        super(privateKey);
    }

    @Override
    protected byte[] doGetSignature(byte[] csrInfo) throws FunctionFailedException {
        ECDSASignature signatureResponse = CertificateManagement.signCertificateRequestECDSA(getPrivateKey(),
                CertificateRequestFormat.PKCS10, csrInfo, getDigestAlgorithmMapping().getECDSASignatureAlgorithm());
        return signatureResponse.toASN1DER();
    }

    @Override
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return getDigestAlgorithmMapping().getEcdsaAlgorithmIdentifier();
    }
}