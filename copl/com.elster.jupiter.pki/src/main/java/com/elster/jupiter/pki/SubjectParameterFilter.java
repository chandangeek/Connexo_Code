package com.elster.jupiter.pki;

import com.elster.jupiter.rest.util.JsonQueryFilter;

public class SubjectParameterFilter  extends AbstractParameter {

    public SubjectParameterFilter(SecurityManagementService securityManagementService, JsonQueryFilter filter) {
        searchParam = "subject";
        service = securityManagementService;
        jsonFilter = filter;

        setProperties();
    }
}