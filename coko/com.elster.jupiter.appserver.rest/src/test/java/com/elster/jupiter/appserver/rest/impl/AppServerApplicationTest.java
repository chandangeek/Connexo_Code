/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.cron.CronExpressionParser;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import java.nio.file.FileSystem;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class AppServerApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    protected AppService appService;
    @Mock
    protected MessageService messageService;
    @Mock
    protected FileImportService fileImportService;
    @Mock
    protected RestQueryService restQueryService;
    @Mock
    protected CronExpressionParser cronExpressionParser;
    @Mock
    protected DataExportService dataExportService;
    @Mock
    static FileSystem fileSystem;
    @Mock
    static SecurityContext securityContext;
    @Mock
    EndPointConfigurationService endPointConfigurationService;
    @Mock
    WebServicesService webServicesService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;


    @Override
    protected Application getApplication() {
        when(thesaurus.join(any(Thesaurus.class))).thenReturn(thesaurus);

        AppServerApplication application = new AppServerApplication();
        application.setAppService(appService);
        application.setFileImportService(fileImportService);
        application.setMessageService(messageService);
        application.setRestQueryService(restQueryService);
        application.setTransactionService(transactionService);
        application.setCronExpressionParser(cronExpressionParser);
        application.setNlsService(nlsService);
        application.setDataExportService(dataExportService);
        application.setEndPointConfigurationService(endPointConfigurationService);
        application.setWebServicesService(webServicesService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        application.setFileSystem(fileSystem);
        return application;
    }
}
