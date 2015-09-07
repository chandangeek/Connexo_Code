package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;

@Path("/processors")
public class FormattersResource {

    private final DataExportService dataExportService;
    private final Thesaurus thesaurus;
    private final PropertyUtils propertyUtils;

    @Inject
    public FormattersResource(DataExportService dataExportService, Thesaurus thesaurus, PropertyUtils propertyUtils) {
        this.dataExportService = dataExportService;
        this.thesaurus = thesaurus;
        this.propertyUtils = propertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public ProcessorInfos getAvailableFormatters(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        if (parameters.containsKey("selector")) {
            return getAvailableFormatters(parameters.get("selector").get(0));
        }
        return getAllAvailableFormatters();
    }

    private ProcessorInfos getAllAvailableFormatters() {
        return toInfos(dataExportService.getAvailableFormatters());
    }

    private ProcessorInfos toInfos(List<DataFormatterFactory> formatters) {
        ProcessorInfos infos = new ProcessorInfos();
        for (DataFormatterFactory processor : formatters) {
            infos.add(processor.getName(), thesaurus.getStringBeyondComponent(processor.getName(), processor.getName()),
                    propertyUtils.convertPropertySpecsToPropertyInfos(processor.getPropertySpecs()));
        }
        infos.total = formatters.size();
        return infos;
    }

    private ProcessorInfos getAvailableFormatters(String selector) {
        List<DataFormatterFactory> dataFormatterFactories = dataExportService.getDataSelectorFactory(selector)
                .map(selectorFactory -> dataExportService.formatterFactoriesMatching(selectorFactory))
                .orElseGet(Collections::emptyList);
        return toInfos(dataFormatterFactories);
    }

}
