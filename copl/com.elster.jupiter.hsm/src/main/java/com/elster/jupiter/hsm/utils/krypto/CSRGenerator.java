/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.utils.krypto;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.ECDSASignature;
import com.atos.worldline.jss.api.cardmanagement.SignatureResponse;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

/**
 * Created by H216758 on 10/9/2018.
 */
public class CSRGenerator {

    private static final Logger LOGGER = Logger.getLogger(CSRGenerator.class.getName());
    private static final String ERR_RETRIEVE_PUBLIC_KEY_INFO = "Failed to retrieve subject's public key info ";
    private static final String ERR_INVALID_KEY_SPECIFICATION = "Invalid key specification ";

    public byte[] generate(String hsmLabel, String x500NameString) {
        try {
            KeyLabel keyLabel = new KeyLabel(hsmLabel);
            LOGGER.info("keyLabel generated: " + keyLabel);
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(Asymmetric.retrieveSubjectPublicKeyInfo(keyLabel));
            LOGGER.info("SubjectPublicKeyInfo generated, algorithm ID: " + publicKeyInfo.getAlgorithm().getAlgorithm().getId() + " ASN1: " + publicKeyInfo.toASN1Primitive());
            X500Name x500Name = new X500Name(x500NameString);
            PKCS10CertificationRequestBuilder builder = getBuilder(x500Name, publicKeyInfo);
            return builder.build(JssContentSignerFactory.INSTANCE.get().getSigner(keyLabel, publicKeyInfo.getAlgorithm())).getEncoded();
        } catch (FunctionFailedException e) {
            LOGGER.info(ERR_RETRIEVE_PUBLIC_KEY_INFO + e);
            throw new IllegalArgumentException(ERR_RETRIEVE_PUBLIC_KEY_INFO, e);
        } catch (InvalidKeySpecException e) {
            LOGGER.info(ERR_INVALID_KEY_SPECIFICATION + e);
            throw new IllegalArgumentException(ERR_INVALID_KEY_SPECIFICATION, e);
        } catch (IOException e) {
            LOGGER.info("Can't encode CSR!");
            throw new IllegalArgumentException(e);
        }
    }

    protected PKCS10CertificationRequestBuilder getBuilder(X500Name x500Name, SubjectPublicKeyInfo publicKeyInfo) {
        return new PKCS10CertificationRequestBuilder(x500Name, publicKeyInfo);
    }

}
