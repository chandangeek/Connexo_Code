package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/importers")
public class FileImporterResource {

    private final FileImportService fileImportService;
    private final FileImporterInfoFactory fileImporterInfoFactory;

    @Inject
    public FileImporterResource(FileImportService fileImportService, FileImporterInfoFactory fileImporterInfoFactory) {
        this.fileImportService = fileImportService;
        this.fileImporterInfoFactory = fileImporterInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES})
    public PagedInfoList getImporters(@BeanParam JsonQueryParameters queryParameters, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName) {
        List<FileImporterFactory> importers = fileImportService.getAvailableImporters(applicationName);
        return PagedInfoList.fromCompleteList("fileImporters", importers.stream().map(fileImporterInfoFactory::asInfo).collect(toList()), queryParameters);
    }
}
