package com.elster.jupiter.pki;

import com.elster.jupiter.rest.util.JsonQueryFilter;

public class AliasParameterFilter extends AbstractParameter {

    public AliasParameterFilter(SecurityManagementService securityManagementService, JsonQueryFilter filter) {
        searchParam = "alias";
        service = securityManagementService;
        jsonFilter = filter;

        setProperties();
    }
}
