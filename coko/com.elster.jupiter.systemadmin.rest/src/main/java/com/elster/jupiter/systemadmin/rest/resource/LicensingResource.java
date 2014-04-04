package com.elster.jupiter.systemadmin.rest.resource;

import com.elster.jupiter.systemadmin.Properties;
import com.elster.jupiter.systemadmin.rest.response.ActionInfo;
import com.elster.jupiter.systemadmin.rest.response.LicenseInfo;
import com.elster.jupiter.systemadmin.rest.response.LicenseListInfo;
import com.elster.jupiter.systemadmin.rest.response.RootEntity;
import com.elster.jupiter.systemadmin.rest.transations.UpgradeLicenseTransaction;
import com.elster.jupiter.systemadmin.rest.transations.UploadLicenseTransaction;
import com.google.common.base.Optional;
import org.glassfish.jersey.media.multipart.*;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.SignedObject;
import java.util.*;

@Path("/license")
public class LicensingResource extends BaseResource {
    public LicensingResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LicenseListInfo getLicenseList(@QueryParam("start") @DefaultValue("0") int start, @QueryParam("limit") @DefaultValue("10") int limit) {

        List resultList  = new ArrayList<>();
        getLicensingService().genereteLicFile();
       // List<Properties> props = getLicensingService().getLicenseList();
        return new LicenseListInfo(getLicensingService().getLicenseList());
    }

    @GET
    @Path("/{tag}")
    @Produces(MediaType.APPLICATION_JSON)
    public RootEntity getLicenseById(@PathParam("tag") String tag) {
        Optional <Properties> propRef = getLicensingService().getLicensedValuesForApplication(tag);
        LicenseInfo info = new LicenseInfo();
        if (propRef.isPresent()) {
            info = new LicenseInfo(propRef.get());
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return new RootEntity<LicenseInfo>(info);
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadLicense(@FormDataParam("application") String application,
                                  @FormDataParam("uploadField") InputStream fileInputStream,
                                  @FormDataParam("uploadField") FormDataContentDisposition contentDispositionHeader) {
        SignedObject signedObject = null;
        Response response = null;
        try {
            ObjectInputStream serializedObject = new ObjectInputStream(fileInputStream);
            signedObject = (SignedObject) serializedObject.readObject();
            serializedObject.close();
        } catch (Exception ex) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        try {
            //Set<String> result = getLicensingService().addLicense(signedObject);
            if (application != null) {
                getTransactionService().execute(new UpgradeLicenseTransaction(getLicensingService(), signedObject, application));
            } else {
                getTransactionService().execute(new UploadLicenseTransaction(getLicensingService(), signedObject));
            }
            Set<String> result = getLicensingService().getApplicationKey(signedObject);
            response = Response.status(Response.Status.OK).entity(new RootEntity<ActionInfo>(new ActionInfo().setSuccess(result))).build();
        } catch (Exception e) {
            String message = contentDispositionHeader.getFileName() + " file upload has failed. " + e.getMessage();
            response = Response.status(UNPROCESSIBLE_ENTITY).entity(new RootEntity<ActionInfo>(new ActionInfo().setFailure(message))).build();
        }
        return response;
    }
}
