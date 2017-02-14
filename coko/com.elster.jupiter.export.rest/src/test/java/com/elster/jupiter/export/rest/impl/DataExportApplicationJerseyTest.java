/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;

import javax.ws.rs.core.Application;

import org.mockito.Answers;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class DataExportApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected DataExportService dataExportService;
    @Mock
    protected MeteringService meteringService;
    @Mock
    protected MeteringGroupsService meteringGroupsService;
    @Mock
    protected RestQueryService restQueryService;
    @Mock
    protected TimeService timeService;
    @Mock
    protected AppService appService;
    @Mock
    protected PropertyValueInfoService propertyValueInfoService;

    @Override
    protected Application getApplication() {
        when(thesaurus.join(any())).thenReturn(thesaurus);
        DataExportApplication application = new DataExportApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setDataExportService(dataExportService);
        application.setMeteringService(meteringService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setTimeService(timeService);
        application.setAppService(appService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        return application;
    }
}
