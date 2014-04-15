package com.elster.jupiter.systemadmin.rest.transations;

import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class UploadLicenseTransaction implements Transaction<ActionInfo> {
    private volatile LicenseService licenseService;
    private volatile NlsService nlsService;
    private SignedObject object;

    @Inject
    public UploadLicenseTransaction(LicenseService licenseService, NlsService nlsService, SignedObject object) {
        this.licenseService = licenseService;
        this.nlsService = nlsService;
        this.object = object;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo info = new ActionInfo();
        try {
            Set<String> appSet = licenseService.addLicense(object);
            Set<String> translatedKeys = new LinkedHashSet<>();
            for(String app : appSet) {
                translatedKeys.add(nlsService.getThesaurus(app, Layer.REST).getString(app, app));
            }
            info.setSuccess(translatedKeys);
        } catch (Exception ex) {
            info.setFailure(ex.getMessage());
            throw new WebApplicationException(Response.status(BaseResource.UNPROCESSIBLE_ENTITY).entity(new RootEntity<ActionInfo>(info)).build());
        }
        return info;
    }
}
