/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.gogo.HsmRenewGogo", service = {HsmRenewGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=renewKey"}, immediate = true)

/**
 * This class is just for test purpose for the time being
 */
public class HsmRenewGogo {

    private volatile HsmEnergyService engService;

    public HsmEncryptedKey renewKey(String deviceKey, String signKeyLabel, String deviceKeyLabel) throws HsmBaseException {
        return this.engService.renewKey(deviceKey.getBytes(), signKeyLabel, deviceKeyLabel);
    }

    @Reference
    public void setHsmEnergyService(HsmEnergyService energyService) {
        this.engService = energyService;
    }

}


