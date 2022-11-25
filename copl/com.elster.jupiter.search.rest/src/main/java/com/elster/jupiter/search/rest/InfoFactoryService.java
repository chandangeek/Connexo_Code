/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest;

import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.search.SearchDomain;

import aQute.bnd.annotation.ProviderType;

/**
 * This service allows fetching factories in a generic way, based on the class of the objects returned from a search query.
 * Created by bvn on 6/4/15.
 */
@ProviderType
public interface InfoFactoryService {
    InfoFactory<?> getInfoFactoryFor(SearchDomain domainObject);
}
