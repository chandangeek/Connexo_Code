package com.elster.jupiter.pki;

import com.elster.jupiter.rest.util.JsonQueryFilter;

public class KeyUsagesParameterFilter extends AbstractParameter {

    public KeyUsagesParameterFilter(SecurityManagementService securityManagementService, JsonQueryFilter filter) {
        searchParam = "keyUsages";
        service = securityManagementService;
        jsonFilter = filter;

        setProperties();
    }
}