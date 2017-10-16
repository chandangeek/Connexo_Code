package com.elster.jupiter.pki;

import com.elster.jupiter.pki.AbstractParameter;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.JsonQueryFilter;

public class IssuerParameterFilter extends AbstractParameter {

    public IssuerParameterFilter(PkiService service, JsonQueryFilter filter) {
        searchParam = "issuer";
        pkiService = service;
        jsonFilter = filter;

        setProperties();
    }
}
