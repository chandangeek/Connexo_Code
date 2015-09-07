package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.license.License;
import com.elster.jupiter.license.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.ActionInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.LicenseInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.LicenseListInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.RootEntity;
import com.elster.jupiter.systemadmin.rest.imp.transations.UploadLicenseTransaction;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.SignedObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/license")
public class LicenseResource extends BaseResource {
    private Thesaurus thesaurus;

    public LicenseResource() {
    }

    @Inject
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_LICENSE)
    public LicenseListInfo getLicenseList() {
        List<License> resultList = new ArrayList<>();
        List<String> applKeyList = getLicenseService().getLicensedApplicationKeys();
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_LICENSE)
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
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed(Privileges.Constants.UPLOAD_LICENSE)
    public Response uploadLicense(@FormDataParam("uploadField") InputStream fileInputStream,
                                  @FormDataParam("uploadField") FormDataContentDisposition contentDispositionHeader) {
        SignedObject signedObject = null;
        try {
            ObjectInputStream serializedObject = new ObjectInputStream(fileInputStream);
            signedObject = (SignedObject) serializedObject.readObject();
            serializedObject.close();
        } catch (Exception ex) {
            throw new WebApplicationException(Response.status(BaseResource.UNPROCESSIBLE_ENTITY).entity(getJsonService().serialize(new ConstraintViolationInfo(thesaurus).from(new InvalidLicenseFileException()))).build());
        }

        ActionInfo info = getTransactionService().execute(new UploadLicenseTransaction(getLicenseService(), getNlsService(), getJsonService(), signedObject));
        return Response.status(Response.Status.OK).entity(getJsonService().serialize(info)).build();
    }
}
