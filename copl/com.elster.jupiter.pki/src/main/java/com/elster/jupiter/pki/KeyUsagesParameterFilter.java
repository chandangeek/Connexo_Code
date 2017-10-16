package com.elster.jupiter.pki;

import com.elster.jupiter.pki.AbstractParameter;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.JsonQueryFilter;

public class KeyUsagesParameterFilter extends AbstractParameter {

    public KeyUsagesParameterFilter(PkiService service, JsonQueryFilter filter) {
        searchParam = "keyUsages";
        pkiService = service;
        jsonFilter = filter;

        setProperties();
    }
}