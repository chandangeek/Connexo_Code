package com.elster.jupiter.systemadmin.rest.transations;

import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.systemadmin.rest.resource.BaseResource;
import com.elster.jupiter.systemadmin.rest.response.ActionInfo;
import com.elster.jupiter.systemadmin.rest.response.RootEntity;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.SignedObject;
import java.util.LinkedHashSet;
import java.util.Set;

public class UploadLicenseTransaction implements Transaction<ActionInfo> {
    private volatile LicenseService licenseService;
    private volatile NlsService nlsService;
    private volatile JsonService jsonService;
    private SignedObject object;

    @Inject
    public UploadLicenseTransaction(LicenseService licenseService, NlsService nlsService, JsonService jsonService, SignedObject object) {
        this.licenseService = licenseService;
        this.nlsService = nlsService;
        this.jsonService = jsonService;
        this.object = object;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo info = new ActionInfo();
        try {
            Set<String> appSet = licenseService.addLicense(object);
            Set<String> translatedKeys = new LinkedHashSet<>();
            for (String app : appSet) {
                translatedKeys.add(nlsService.getThesaurus(app, Layer.REST).getString(app, app));
            }
            info.setSuccess(translatedKeys);
        } catch (Exception ex) {
            info.setErrors(ex.getMessage());
            throw new WebApplicationException(Response.status(BaseResource.UNPROCESSIBLE_ENTITY).entity(jsonService.serialize(info)).build());
        }
        return info;
    }
}
