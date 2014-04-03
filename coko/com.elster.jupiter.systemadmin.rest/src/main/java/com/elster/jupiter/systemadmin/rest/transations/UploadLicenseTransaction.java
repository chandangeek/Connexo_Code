package com.elster.jupiter.systemadmin.rest.transations;

import com.elster.jupiter.systemadmin.LicensingService;
import com.elster.jupiter.systemadmin.rest.response.ActionInfo;
import com.elster.jupiter.transaction.Transaction;

import java.security.SignedObject;

public class UploadLicenseTransaction implements Transaction<ActionInfo> {
    private volatile LicensingService licensingService;
    private SignedObject object;

    public UploadLicenseTransaction(LicensingService licensingService, SignedObject object) {
        this.licensingService = licensingService;
        this.object = object;
    }

    @Override
    public ActionInfo perform() throws RuntimeException {
        if(!licensingService.addLicense(object)) {
            throw new RuntimeException();
        }
        return null;
    }
}
