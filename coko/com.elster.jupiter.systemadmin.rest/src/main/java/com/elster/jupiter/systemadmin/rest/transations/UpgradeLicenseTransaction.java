package com.elster.jupiter.systemadmin.rest.transations;

import com.elster.jupiter.systemadmin.LicensingService;
import com.elster.jupiter.systemadmin.rest.response.ActionInfo;
import com.elster.jupiter.transaction.Transaction;

import java.security.SignedObject;

public class UpgradeLicenseTransaction implements Transaction<ActionInfo> {
    private volatile LicensingService licensingService;
    private SignedObject object;
    private String key;

    public UpgradeLicenseTransaction(LicensingService licensingService, SignedObject object, String key) {
        this.licensingService = licensingService;
        this.object = object;
        this.key = key;
    }

    @Override
    public ActionInfo perform() throws RuntimeException {
        if(!licensingService.upgradeLicense(object, key)) {
            throw new RuntimeException();
        }
        return null;
    }
}
