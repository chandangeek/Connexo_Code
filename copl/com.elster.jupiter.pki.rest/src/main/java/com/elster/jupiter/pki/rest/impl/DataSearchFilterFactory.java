package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.rest.util.JsonQueryFilter;

import java.util.Optional;

public class DataSearchFilterFactory {

    public PkiService.DataSearchFilter asFilter(JsonQueryFilter jsonQueryFilter, Optional<TrustStore> trustStore) {
        PkiService.DataSearchFilter dataSearchFilter = new PkiService.DataSearchFilter();

        JsonFilterParametersBean params = new JsonFilterParametersBean(jsonQueryFilter);

        dataSearchFilter.trustStore = trustStore;
        dataSearchFilter.alias = params.getStringList("alias");
        dataSearchFilter.subject = params.getStringList("subject");
        dataSearchFilter.issuer = params.getStringList("issuer");
        dataSearchFilter.keyUsages = params.getStringList("keyUsages");
        dataSearchFilter.extendedKeyUsages = params.getStringList("extendedKeyUsages");
        dataSearchFilter.intervalFrom = params.getInstant("intervalFrom");
        dataSearchFilter.intervalTo = params.getInstant("intervalTo");

        return dataSearchFilter;
    }
}
