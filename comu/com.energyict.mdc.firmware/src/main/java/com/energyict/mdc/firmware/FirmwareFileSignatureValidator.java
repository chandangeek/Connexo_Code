/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

public interface FirmwareFileSignatureValidator {
    String SHA256_WITH_ECDSA_ALGORITHM = "SHA256withECDSA";
    String SHA384_WITH_ECDSA_ALGORITHM = "SHA384withECDSA";
    Integer SECP256R1_CURVE_SIGNATURE_LENGTH = 64;
    Integer SECP384R1_CURVE_SIGNATURE_LENGTH = 96;

    void validateSignature();

}
