/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.custom;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;


@Component(
        name = "com.elster.jupiter.export.custom",
        service = {CustomDataExportService.class},
        property = "name=" + "DEC",
        immediate = true)
public class CustomDataExportService {
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile OrmService ormService;
    private volatile DataExportService dataExportService;

    public CustomDataExportService() {
    }

    @Inject
    public CustomDataExportService(BundleContext context, OrmService ormService, NlsService nlsService) {
        setOrmService(ormService);
        setDataExportService(dataExportService);
        setNlsService(nlsService);
        activate(context);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.getDataModel("DES").get();
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Activate
    public final void activate(BundleContext context) {
        try {
            dataExportService.addSelector(new CustomDataSelectorFactory(thesaurus), ImmutableMap.of(DataExportService.DATA_TYPE_PROPERTY, DataExportService.CUSTOM_READING_DATA_TYPE));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DataExportService.COMPONENTNAME, Layer.DOMAIN);
    }
}
