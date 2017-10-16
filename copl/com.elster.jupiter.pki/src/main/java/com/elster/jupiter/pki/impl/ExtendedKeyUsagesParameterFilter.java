package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.AbstractParameter;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.JsonQueryFilter;

public class ExtendedKeyUsagesParameterFilter extends AbstractParameter {

    public ExtendedKeyUsagesParameterFilter(PkiService service, JsonQueryFilter filter) {
        searchParam = "extendedKeyUsages";
        pkiService = service;
        jsonFilter = filter;

        setProperties();
    }
}