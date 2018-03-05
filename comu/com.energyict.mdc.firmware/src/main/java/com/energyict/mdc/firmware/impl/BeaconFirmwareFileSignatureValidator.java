/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.energyict.mdc.firmware.FirmwareFileSignatureValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class BeaconFirmwareFileSignatureValidator implements FirmwareFileSignatureValidator {
    private Thesaurus thesaurus;
    private SecurityAccessor securityAccessor;
    private byte[] firmwareFile;

    private static final int BUFFER_SIZE = 1024;

    BeaconFirmwareFileSignatureValidator(Thesaurus thesaurus, SecurityAccessor securityAccessor, byte[] firmwareFile) {
        this.thesaurus = thesaurus;
        this.securityAccessor = securityAccessor;
        this.firmwareFile = firmwareFile;

    }

    @Override
    public void validateSignature() throws SignatureValidationFailedException {
        if (securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) securityAccessor.getActualValue().get();
            if (certificateWrapper.getCertificate().isPresent()) {
                X509Certificate x509Certificate = certificateWrapper.getCertificate().get();
                String sigAlgName = x509Certificate.getSigAlgName(); //SHA256withECDSA (suite 1) or SHA384withECDSA (suite 2)
                if (sigAlgName == null || sigAlgName.isEmpty()) {
                    return;
                }
                if (!sigAlgName.contains(SHA256_WITH_ECDSA_ALGORITHM) || !sigAlgName.contains(SHA384_WITH_ECDSA_ALGORITHM)) {
                    throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VALIDATION_FAILED, sigAlgName);
                }
                Integer signatureLength = sigAlgName.contains(SHA256_WITH_ECDSA_ALGORITHM) ? SECP256R1_CURVE_SIGNATURE_LENGTH : SECP384R1_CURVE_SIGNATURE_LENGTH;
                try {
                    Signature sig = Signature.getInstance(sigAlgName);
                    sig.initVerify(x509Certificate);
                    addDataToVerify(sig, firmwareFile, signatureLength);
                    byte[] signature = getFileSignature(firmwareFile, signatureLength);
                    boolean validationResult = sig.verify(signature);
                    if (!validationResult) {
                        throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VALIDATION_FAILED, validationResult);
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException e) {
                    throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VALIDATION_FAILED, e);
                }
            }
        }
    }

    private void addDataToVerify(Signature signature, byte[] firmwareFile, Integer signatureLength) throws IOException, SignatureException {
        int length = firmwareFile.length - signatureLength;

        byte[] buffer = new byte[BUFFER_SIZE];
        try (ByteArrayInputStream stream = new ByteArrayInputStream(firmwareFile)) {

            int currentBytesRead = stream.read(buffer);
            int totalBytesRead = 0;

            while (currentBytesRead != -1 && (length == -1 || totalBytesRead < length)) {
                int bytesToConsider;

                if (length != -1 && totalBytesRead + currentBytesRead > length) {
                    bytesToConsider = length - totalBytesRead;
                } else {
                    bytesToConsider = currentBytesRead;
                }

                signature.update(buffer, 0, bytesToConsider);

                totalBytesRead += currentBytesRead;
                currentBytesRead = stream.read(buffer);
            }
        }
    }

    private byte[] getFileSignature(byte[] firmwareFile, Integer signatureLength) {
        byte[] signature = new byte[signatureLength];
        System.arraycopy(firmwareFile, firmwareFile.length - signatureLength, signature, 0, signatureLength);
        return trim(signature);
    }

    private byte[] trim(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }
        return Arrays.copyOf(bytes, i + 1);
    }


}


