/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.time.TimeService;

import javax.ws.rs.core.Application;
import java.time.Clock;

import org.mockito.Answers;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public abstract class DataExportApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected DataExportService dataExportService;
    @Mock
    protected MeteringService meteringService;
    @Mock
    protected MetrologyConfigurationService metrologyConfigurationService;
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
    @Mock
    protected Clock clock;
    @Mock
    protected EndPointConfigurationService endPointConfigurationService;
    @Mock
    protected OrmService ormService;

    @Override
    protected Application getApplication() {
        when(thesaurus.join(any())).thenReturn(thesaurus);
        DataExportApplication application = new DataExportApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setDataExportService(dataExportService);
        application.setMeteringService(meteringService);
        application.setMetrologyConfigurationService(metrologyConfigurationService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setTimeService(timeService);
        application.setAppService(appService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        application.setClock(clock);
        application.setEndPointConfigurationService(endPointConfigurationService);
        application.setOrmService(ormService);
        return application;
    }
}
