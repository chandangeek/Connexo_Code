/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.util.Optional;

public interface DeviceCSR {

    long getId();

    Device getDevice();

    Optional<PKCS10CertificationRequest> getCSR();

    void save();
}
