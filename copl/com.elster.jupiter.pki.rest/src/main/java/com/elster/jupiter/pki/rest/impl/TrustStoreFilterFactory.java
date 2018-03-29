package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.JsonQueryFilter;

import java.util.Optional;

public class TrustStoreFilterFactory {

    public SecurityManagementService.TrustStoreFilter asFilter(JsonQueryFilter jsonQueryFilter){
        JsonFilterParametersBean params = new JsonFilterParametersBean(jsonQueryFilter);
        SecurityManagementService.TrustStoreFilter filter = new SecurityManagementService.TrustStoreFilter();
        filter.nameContains = params.getString("nameContains");
        return filter;
    }

    public SecurityManagementService.TrustStoreFilter asLike(String like){
        SecurityManagementService.TrustStoreFilter filter = new SecurityManagementService.TrustStoreFilter();
        filter.nameContains = Optional.ofNullable(like);
        return filter;
    }
}
