/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class is just for test purpose for the time being
 */
@Component(name = "com.elster.jupiter.hsm.gogo.HsmRenewGogo", service = {HsmRenewGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=renewKey"}, immediate = true)
public class HsmRenewGogo {

    private volatile HsmEnergyService engService;

    public HsmEncryptedKey renewKey(String actualKey, String actualLabel, String renewLabel) throws HsmBaseException {
        return this.engService.renewKey(new RenewKeyRequest(actualKey.getBytes(), actualLabel, renewLabel));
    }

    @Reference
    public void setHsmEnergyService(HsmEnergyService energyService) {
        this.engService = energyService;
    }

}


