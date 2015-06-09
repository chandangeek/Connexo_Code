package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.search.rest.InfoFactoryService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.rest.MessageSeeds;
import com.elster.jupiter.util.exception.MessageSeed;
import javax.ws.rs.core.Application;
import org.mockito.Mock;

public class SearchApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    protected SearchService searchService;
    @Mock
    protected InfoFactoryService infoFactoryService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        SearchApplication searchApplication = new SearchApplication();
        searchApplication.setSearchService(searchService);
        searchApplication.setNlsService(nlsService);
        searchApplication.setInfoFactoryService(infoFactoryService);
        return searchApplication;
    }
}