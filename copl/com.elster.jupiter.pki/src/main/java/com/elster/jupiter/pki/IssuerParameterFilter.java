package com.elster.jupiter.pki;

import com.elster.jupiter.rest.util.JsonQueryFilter;

public class IssuerParameterFilter extends AbstractParameter {

    public IssuerParameterFilter(SecurityManagementService securityManagementService, JsonQueryFilter filter) {
        searchParam = "issuer";
        service = securityManagementService;
        jsonFilter = filter;

        setProperties();
    }
}
