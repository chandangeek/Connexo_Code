package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.AbstractParameter;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.JsonQueryFilter;

public class SubjectParameterFilter  extends AbstractParameter {

    public SubjectParameterFilter(PkiService service, JsonQueryFilter filter) {
        searchParam = "subject";
        pkiService = service;
        jsonFilter = filter;

        setProperties();
    }
}