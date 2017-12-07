package com.elster.jupiter.pki;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.rest.util.JsonQueryFilter;

public abstract class AbstractParameter {
    public JsonQueryFilter jsonFilter;
    public SecurityManagementService service;
    public TrustStore trustStore;
    public String searchParam;
    public String searchValue;
    public Long trustStoreId;

    public void setProperties() {
        searchValue = null;
        trustStoreId = null;

        if ( jsonFilter.hasFilters()) {
            searchValue = jsonFilter.getString(searchParam);
        }
        if ( jsonFilter.hasProperty("trustStore")) {
            trustStoreId = jsonFilter.getLong("trustStore");
        }
        if (trustStoreId != null) {
            trustStore = service.findTrustStore(trustStoreId)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_TRUSTSTORE, "trustStore"));
        }
        if (searchValue == null || searchValue.isEmpty()) {
            searchValue = "*";
        }
        if (searchValue != null && !searchValue.isEmpty()) {
            if (!searchValue.contains("*") && !searchValue.contains("?")) {
                searchValue = "*" + searchValue + "*";
            }
        }
    }
}
