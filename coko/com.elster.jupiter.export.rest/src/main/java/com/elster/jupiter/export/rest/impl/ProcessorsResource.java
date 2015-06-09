package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 6/11/2014
 * Time: 13:48
 */
@Path("/processors")
public class ProcessorsResource {

    private final DataExportService dataExportService;
    private final Thesaurus thesaurus;
    private final PropertyUtils propertyUtils;

    @Inject
    public ProcessorsResource(DataExportService dataExportService, Thesaurus thesaurus, PropertyUtils propertyUtils) {
        this.dataExportService = dataExportService;
        this.thesaurus = thesaurus;
        this.propertyUtils = propertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DATA_EXPORT_TASK, Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK})
    public ProcessorInfos getAvailableProcessors(@Context UriInfo uriInfo) {
        ProcessorInfos infos = new ProcessorInfos();
        List<DataProcessorFactory> processors = dataExportService.getAvailableProcessors();
        for (DataProcessorFactory processor : processors) {
            infos.add(processor.getName(), thesaurus.getStringBeyondComponent(processor.getName(), processor.getName()),
                    propertyUtils.convertPropertySpecsToPropertyInfos(processor.getPropertySpecs()));
        }
        infos.total = processors.size();
        return infos;
    }
}
