package com.elster.jupiter.search.rest;

import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.search.SearchDomain;

/**
 * This service allows fetching factories in a generic way, based on the class of the objects returned from a search query.
 * Created by bvn on 6/4/15.
 */
public interface InfoFactoryService {

    public InfoFactory getInfoFactoryFor(SearchDomain domainObject);
}
