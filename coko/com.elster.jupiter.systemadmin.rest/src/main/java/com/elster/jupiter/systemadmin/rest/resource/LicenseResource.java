package com.elster.jupiter.systemadmin.rest.resource;

import com.elster.jupiter.license.License;
import com.elster.jupiter.systemadmin.rest.response.ActionInfo;
import com.elster.jupiter.systemadmin.rest.response.LicenseInfo;
import com.elster.jupiter.systemadmin.rest.response.LicenseListInfo;
import com.elster.jupiter.systemadmin.rest.response.RootEntity;
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
public class LicenseResource extends BaseResource {
    public LicenseResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LicenseListInfo getLicenseList() {
        List<License> resultList  = new ArrayList<>();
        List<String> applKeyList =  getLicenseService().getLicensedApplicationKeys();
        for (String key : applKeyList) {
            Optional<License> licRef = getLicenseService().getLicenseForApplication(key);
            if (licRef.isPresent()) {
                resultList.add(licRef.get());
            }
        }
        return new LicenseListInfo(getNlsService(), resultList);
    }

    @GET
    @Path("/{applicationkey}")
    @Produces(MediaType.APPLICATION_JSON)
    public RootEntity getLicenseById(@PathParam("applicationkey") String tag) {
        Optional<License> licenseRef = getLicenseService().getLicenseForApplication(tag);
        LicenseInfo info = new LicenseInfo();
        if (licenseRef.isPresent()) {
            info = new LicenseInfo(getNlsService(), licenseRef.get());
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return new RootEntity<LicenseInfo>(info);
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadLicense(@FormDataParam("uploadField") InputStream fileInputStream,
                                  @FormDataParam("uploadField") FormDataContentDisposition contentDispositionHeader) {
        SignedObject signedObject = null;
        try {
            ObjectInputStream serializedObject = new ObjectInputStream(fileInputStream);
            signedObject = (SignedObject) serializedObject.readObject();
            serializedObject.close();
        } catch (Exception ex) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        ActionInfo info =  getTransactionService().execute(new UploadLicenseTransaction(getLicenseService(), getNlsService(), signedObject));
        return Response.status(Response.Status.OK).entity(new RootEntity<ActionInfo>(info)).build();
    }
}
