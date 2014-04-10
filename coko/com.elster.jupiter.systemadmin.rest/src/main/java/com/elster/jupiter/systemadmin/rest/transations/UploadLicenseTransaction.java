package com.elster.jupiter.systemadmin.rest.transations;

import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.systemadmin.rest.resource.BaseResource;
import com.elster.jupiter.systemadmin.rest.response.ActionInfo;
import com.elster.jupiter.systemadmin.rest.response.RootEntity;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.security.SignedObject;
import java.util.Set;

public class UploadLicenseTransaction implements Transaction<ActionInfo> {
    private volatile LicenseService licenseService;
    private SignedObject object;

    @Inject
    public UploadLicenseTransaction(LicenseService licenseService, SignedObject object) {
        this.licenseService = licenseService;
        this.object = object;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo info = new ActionInfo();
        try {
            Set<String> appSet = licenseService.addLicense(object);
            if (!appSet.isEmpty()) {
                info.setSuccess(appSet);
            }
        } catch (Exception ex) {
            info.setFailure(ex.getMessage());
            throw new WebApplicationException(Response.status(BaseResource.UNPROCESSIBLE_ENTITY).entity(new RootEntity<ActionInfo>(info)).build());
        }
        return info;
    }
}
