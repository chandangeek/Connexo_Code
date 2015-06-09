package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.impl.StandardDataSelectorFactory;
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

@Path("/selectors")
public class SelectorsResource {

    private final DataExportService dataExportService;
    private final Thesaurus thesaurus;

    @Inject
    public SelectorsResource(DataExportService dataExportService, Thesaurus thesaurus) {
        this.dataExportService = dataExportService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DATA_EXPORT_TASK, Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK})
    public SelectorInfos getAvailableSelectors(@Context UriInfo uriInfo) {
        SelectorInfos infos = new SelectorInfos();
        List<DataSelectorFactory> selectors = dataExportService.getAvailableSelectors();
        PropertyUtils propertyUtils = new PropertyUtils();
        for (DataSelectorFactory selector : selectors) {
            infos.add(selector.getName(), thesaurus.getStringBeyondComponent(selector.getName(), selector.getDisplayName()),
                    propertyUtils.convertPropertySpecsToPropertyInfos(selector.getPropertySpecs()), selector.isDefault());
        }

        infos.total = selectors.size();
        return infos;
    }
}
