/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.search.SearchCriteriaService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.location.SearchLocationService;
import com.elster.jupiter.search.rest.InfoFactoryService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import org.mockito.Mock;

import javax.ws.rs.core.Application;

public class SearchApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    protected SearchService searchService;
    @Mock
    protected InfoFactoryService infoFactoryService;
    @Mock
    protected SearchLocationService searchLocationService;
    @Mock
    protected SearchCriteriaService searchCriteriaService;
    @Mock
    protected RestQueryService restQueryService;
    @Mock
    ThreadPrincipalService threadPrincipalService;

    @Override
    protected Application getApplication() {
        SearchApplication searchApplication = new SearchApplication();
        searchApplication.setSearchService(searchService);
        searchApplication.setSearchLocationService(searchLocationService);
        searchApplication.setNlsService(nlsService);
        searchApplication.setInfoFactoryService(infoFactoryService);
        searchApplication.setSearchCriteriaService(searchCriteriaService);
        searchApplication.setThreadPrincipalService(threadPrincipalService);
        searchApplication.setRestQueryService(restQueryService);
        return searchApplication;
    }

}