package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.rest.util.JsonQueryFilter;

import java.util.Optional;

public class DataSearchFilterFactory {

    public SecurityManagementService.DataSearchFilter asFilter(JsonQueryFilter jsonQueryFilter, Optional<TrustStore> trustStore) {
        SecurityManagementService.DataSearchFilter dataSearchFilter = new SecurityManagementService.DataSearchFilter();

        JsonFilterParametersBean params = new JsonFilterParametersBean(jsonQueryFilter);

        dataSearchFilter.trustStore = trustStore;
        dataSearchFilter.alias = params.getStringList("alias");
        dataSearchFilter.subject = params.getStringList("subject");
        dataSearchFilter.issuer = params.getStringList("issuer");
        dataSearchFilter.keyUsages = params.getStringList("keyUsages");
        dataSearchFilter.intervalFrom = params.getInstant("intervalFrom");
        dataSearchFilter.intervalTo = params.getInstant("intervalTo");
        dataSearchFilter.aliasContains = params.getString("aliasContains");

        return dataSearchFilter;
    }

    public SecurityManagementService.DataSearchFilter asLikeFilter(String like) {
        SecurityManagementService.DataSearchFilter dataSearchFilter = new SecurityManagementService.DataSearchFilter();
        dataSearchFilter.aliasContains = Optional.ofNullable(like);
        return dataSearchFilter;
    }
}
