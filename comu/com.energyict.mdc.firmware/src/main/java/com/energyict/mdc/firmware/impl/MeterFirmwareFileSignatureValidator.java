/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.energyict.mdc.firmware.FirmwareFileSignatureValidator;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class MeterFirmwareFileSignatureValidator implements FirmwareFileSignatureValidator {

    private Thesaurus thesaurus;
    private SecurityAccessor securityAccessor;
    private byte[] firmwareFile;

    private static final int SIGNATURE_TYPE_BYTE_INDEX = 37;
    private static final int HEADER_SIZE = 256;
    private static final int NO_SIGNATURE = 0;
    private static final int ECDSA_SIGNATURE_TYPE = 4;

    MeterFirmwareFileSignatureValidator(Thesaurus thesaurus, SecurityAccessor securityAccessor, byte[] firmwareFile) {
        this.thesaurus = thesaurus;
        this.securityAccessor = securityAccessor;
        this.firmwareFile = firmwareFile;
    }

    @Override
    public void validateSignature() {
        if (securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) securityAccessor.getActualValue().get();
            if (certificateWrapper.getCertificate().isPresent()) {
                X509Certificate x509Certificate = certificateWrapper.getCertificate().get();
                String sigAlgName = x509Certificate.getSigAlgName(); //SHA256withECDSA (suite 1) or SHA384withECDSA (suite 2)
                if (!sigAlgName.contains(SHA256_WITH_ECDSA_ALGORITHM) || !sigAlgName.contains(SHA384_WITH_ECDSA_ALGORITHM)) {
                    throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VERIFICATION_FAILED);
                }
                Integer signatureLength = sigAlgName.contains(SHA256_WITH_ECDSA_ALGORITHM) ? SECP256R1_CURVE_SIGNATURE_LENGTH : SECP384R1_CURVE_SIGNATURE_LENGTH;
                int signatureType = getSignatureType(firmwareFile);
                if (signatureType == NO_SIGNATURE) {
                    return;
                }
                if (signatureType != ECDSA_SIGNATURE_TYPE) {
                    throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VERIFICATION_FAILED);
                }
                if (firmwareFile.length < HEADER_SIZE) {
                    throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VERIFICATION_FAILED);
                }

                /* verifyHeaderSignature */
                byte[] message = getHeaderMessage(firmwareFile, signatureLength);
                byte[] signature = getHeaderSignature(firmwareFile, signatureLength);
                verifySignature(x509Certificate, message, signature);

                /* verifyImageSignature */
                message = getImageMessage(firmwareFile, signatureLength);
                signature = getImageSignature(firmwareFile, signatureLength);
                verifySignature(x509Certificate, message, signature);
            }
        }
    }


    private byte[] getImageMessage(byte[] firmwareFile, Integer signatureLength) {
        return readBytes(firmwareFile, 0, firmwareFile.length - signatureLength);
    }

    private byte[] getHeaderMessage(byte[] firmwareFile, Integer signatureLength) {
        return readBytes(firmwareFile, 0, HEADER_SIZE - signatureLength);
    }

    private byte[] getHeaderSignature(byte[] firmwareFile, Integer signatureLength) {
        return readBytes(firmwareFile, HEADER_SIZE - signatureLength, signatureLength);
    }

    private byte[] getImageSignature(byte[] firmwareFile, Integer signatureLength) {
        return readBytes(firmwareFile, firmwareFile.length - signatureLength, signatureLength);
    }

    private byte[] readBytes(byte[] firmwareFile, int startIndex, int length) {
        byte[] result = new byte[length];
        System.arraycopy(firmwareFile, startIndex, result, 0, length);
        return result;
    }

    private int getSignatureType(byte[] firmwareFile) {
        return readBytes(firmwareFile, SIGNATURE_TYPE_BYTE_INDEX, 1)[0];
    }

    private void verifySignature(X509Certificate x509Certificate, byte[] message, byte[] signature) {
        try {
            signature = encodeECDSASignatureDER(signature);
            Signature sig = Signature.getInstance(x509Certificate.getSigAlgName());
            sig.initVerify(x509Certificate);
            sig.update(message);
            if (!sig.verify(signature)) {
                throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VERIFICATION_FAILED);
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException e) {
            throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VERIFICATION_FAILED, e);
        }
    }

    private byte[] encodeECDSASignatureDER(byte[] rs) throws IOException {
        if (rs.length < 2) {
            return rs;
        }
        byte[] r = Arrays.copyOfRange(rs, 0, rs.length / 2);
        if (Byte.toUnsignedInt(r[0]) > 0x7f) { // r and s should be encoded as
            // positive integers
            r = new byte[r.length + 1];
            System.arraycopy(rs, 0, r, 1, rs.length / 2);
            r[0] = 0;
        }
        byte[] s = Arrays.copyOfRange(rs, rs.length / 2, rs.length);
        if (Byte.toUnsignedInt(s[0]) > 0x7f) {
            s = new byte[s.length + 1];
            System.arraycopy(rs, rs.length / 2, s, 1, rs.length / 2);
            s[0] = 0;
        }

        try (final DerOutputStream stream = new DerOutputStream()) {
            final DerValue rDerValue = new DerValue(DerValue.tag_Integer, r);
            final DerValue sDerValue = new DerValue(DerValue.tag_Integer, s);

            stream.putSequence(new DerValue[]{rDerValue, sDerValue});

            return stream.toByteArray();
        }
    }
}
