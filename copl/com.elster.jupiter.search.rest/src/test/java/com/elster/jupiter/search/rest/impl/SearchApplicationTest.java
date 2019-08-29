/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.location.SearchLocationService;
import com.elster.jupiter.search.rest.InfoFactoryService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

public class SearchApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    protected SearchService searchService;
    @Mock
    protected InfoFactoryService infoFactoryService;
    @Mock
    protected SearchLocationService searchLocationService;

    @Override
    protected Application getApplication() {
        SearchApplication searchApplication = new SearchApplication();
        searchApplication.setSearchService(searchService);
        searchApplication.setSearchLocationService(searchLocationService);
        searchApplication.setNlsService(nlsService);
        searchApplication.setInfoFactoryService(infoFactoryService);
        return searchApplication;
    }

}