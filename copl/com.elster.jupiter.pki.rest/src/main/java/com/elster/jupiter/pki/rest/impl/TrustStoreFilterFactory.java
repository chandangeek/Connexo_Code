package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.JsonQueryFilter;

public class TrustStoreFilterFactory {

    public SecurityManagementService.TrustStoreFilter asFilter(JsonQueryFilter jsonQueryFilter){
        JsonFilterParametersBean params = new JsonFilterParametersBean(jsonQueryFilter);

        SecurityManagementService.TrustStoreFilter filter = new SecurityManagementService.TrustStoreFilter();
        filter.nameContains = params.getString("nameContains");
        return filter;
    }
}
