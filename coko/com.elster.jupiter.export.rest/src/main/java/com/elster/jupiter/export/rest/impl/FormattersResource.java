/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

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

import static com.elster.jupiter.export.DataExportService.CUSTOM_READINGTYPE_DATA_SELECTOR;
import static com.elster.jupiter.export.DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR;

@Path("/processors")
public class FormattersResource {

    private final DataExportService dataExportService;
    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public FormattersResource(DataExportService dataExportService, PropertyValueInfoService propertyValueInfoService) {
        this.dataExportService = dataExportService;
        this.propertyValueInfoService = propertyValueInfoService;
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
            infos.add(processor.getName(), processor.getDisplayName(),
                    propertyValueInfoService.getPropertyInfos(processor.getPropertySpecs()));
        }
        infos.total = formatters.size();
        return infos;
    }

    private ProcessorInfos getAvailableFormatters(String selector) {
        if (selector.equals(CUSTOM_READINGTYPE_DATA_SELECTOR)) {
            List<DataFormatterFactory> dataFormatterFactories = dataExportService.getDataSelectorFactory(STANDARD_READINGTYPE_DATA_SELECTOR)
                    .map(dataExportService::formatterFactoriesMatching)
                    .orElseGet(Collections::emptyList);
            return toInfos(dataFormatterFactories);
        } else {
            List<DataFormatterFactory> dataFormatterFactories = dataExportService.getDataSelectorFactory(selector)
                    .map(dataExportService::formatterFactoriesMatching)
                    .orElseGet(Collections::emptyList);
            return toInfos(dataFormatterFactories);
        }
    }

}
