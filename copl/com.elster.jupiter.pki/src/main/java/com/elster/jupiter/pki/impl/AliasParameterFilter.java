package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.AbstractParameter;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.JsonQueryFilter;

public class AliasParameterFilter extends AbstractParameter {

    public AliasParameterFilter(PkiService service, JsonQueryFilter filter) {
        searchParam = "alias";
        pkiService = service;
        jsonFilter = filter;

        setProperties();
    }
}
