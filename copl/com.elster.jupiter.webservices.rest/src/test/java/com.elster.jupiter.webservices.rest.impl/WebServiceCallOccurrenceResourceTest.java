/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OccurrenceLogFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServiceCallOccurrenceImpl;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebServiceCallOccurrenceResourceTest extends WebServicesApplicationTest {

    @Mock
    DataModel dataModel;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(user.getPrivileges(anyString())).thenReturn(Collections.singleton(privilege));
    }

    @Test
    public void  testGetAllOccurrences() throws Exception {
        /* Mock privilege */
        when(privilege.getName()).thenReturn(Privileges.Constants.VIEW_WEB_SERVICES);
        when(user.hasPrivilege("INS", Privileges.Constants.VIEW_WEB_SERVICES)).thenReturn(true);

        /* Mock webService*/
        WebService webService = mock(WebService.class);
        when(webService.getProtocol()).thenReturn(WebServiceProtocol.SOAP);
        when(webServicesService.getWebService(anyString())).thenReturn(Optional.of(webService));

        /*Mock endPointConfiguration*/
        OutboundEndPointConfiguration ecpMock = mock(OutboundEndPointConfiguration.class);
        when(ecpMock.getLogLevel()).thenReturn(LogLevel.INFO);
        when(ecpMock.getAuthenticationMethod()).thenReturn(EndPointAuthentication.BASIC_AUTHENTICATION);
        when((ecpMock).getUsername()).thenReturn("USER");
        when((ecpMock).getPassword()).thenReturn("PASSWORD");

        WebServiceCallOccurrence occurrence1 = createOccurrence(
                Instant.now(),
                "Request1",
                ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName(),
                ecpMock
        );
        WebServiceCallOccurrence occurrence2 = createOccurrence(
                Instant.now(),
                "Request2",
                ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName(),
                ecpMock
        );

        occurrence2.setStatus(WebServiceCallOccurrenceStatus.SUCCESSFUL);

        List<WebServiceCallOccurrence> occurrenceList = new ArrayList<>();
        occurrenceList.add(occurrence1);
        occurrenceList.add(occurrence2);

        WebServiceCallOccurrenceFinderBuilder builder = FakeBuilder.initBuilderStub(occurrenceList, WebServiceCallOccurrenceFinderBuilder.class, Finder.class);
        when(webServiceCallOccurrenceService.getWebServiceCallOccurrenceFinderBuilder()).thenReturn(builder);
        Response response = target("/occurrences").request().header("X-CONNEXO-APPLICATION-NAME", "INS").get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("$.occurrences[0].id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.occurrences[0].status")).isEqualTo(WebServiceCallOccurrenceStatus.ONGOING.getName());
        assertThat(jsonModel.<String>get("$.occurrences[0].request")).isEqualTo("Request1");
        assertThat(jsonModel.<String>get("$.occurrences[0].applicationName")).isEqualTo(ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName());

        assertThat(jsonModel.<Integer>get("$.occurrences[1].id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.occurrences[1].status")).isEqualTo(WebServiceCallOccurrenceStatus.SUCCESSFUL.getName());
        assertThat(jsonModel.<String>get("$.occurrences[1].request")).isEqualTo("Request2");
        assertThat(jsonModel.<String>get("$.occurrences[1].applicationName")).isEqualTo(ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName());
    }

    @Test
    public void testGetOccurrence() throws Exception {
        /* Mock privilege */
        when(privilege.getName()).thenReturn(Privileges.Constants.VIEW_WEB_SERVICES);
        when(user.hasPrivilege("MDC", Privileges.Constants.VIEW_WEB_SERVICES)).thenReturn(true);

        /* Mock webService*/
        WebService webService = mock(WebService.class);
        when(webService.getProtocol()).thenReturn(WebServiceProtocol.SOAP);
        when(webServicesService.getWebService(anyString())).thenReturn(Optional.of(webService));

        /*Mock endPointConfiguration*/
        OutboundEndPointConfiguration ecpMock = mock(OutboundEndPointConfiguration.class);
        when(ecpMock.getLogLevel()).thenReturn(LogLevel.INFO);
        when(ecpMock.getAuthenticationMethod()).thenReturn(EndPointAuthentication.BASIC_AUTHENTICATION);
        when((ecpMock).getUsername()).thenReturn("USER");
        when((ecpMock).getPassword()).thenReturn("PASSWORD");

        WebServiceCallOccurrence occurrence = createOccurrence(
                Instant.now(),
                "Request1",
                ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName(),
                ecpMock
        );
        when(webServiceCallOccurrenceService.getEndPointOccurrence((long) 1)).thenReturn(Optional.of(occurrence));

        /*Test for "/endpointconfigurations/1/occurrences" resource is the same. So just one test for two resources */
        Response response = target("/occurrences/1").request().header("X-CONNEXO-APPLICATION-NAME", "MDC").get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.status")).isEqualTo(WebServiceCallOccurrenceStatus.ONGOING.getName());
        assertThat(jsonModel.<String>get("$.request")).isEqualTo("Request1");
        assertThat(jsonModel.<String>get("$.applicationName")).isEqualTo(ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName());
    }

    private EndPointLog mockEndPointLog(long id,
                                        LogLevel level,
                                        String message,
                                        Instant time,
                                        EndPointConfiguration epc,
                                        String stackTrace,
                                        WebServiceCallOccurrence occurrence) {
        EndPointLog log = mock(EndPointLog.class);
        when(log.getId()).thenReturn(id);
        when(log.getLogLevel()).thenReturn(level);
        when(log.getMessage()).thenReturn(message);
        when(log.getTime()).thenReturn(time);
        when(log.getEndPointConfiguration()).thenReturn(epc);
        when(log.getStackTrace()).thenReturn(stackTrace);
        when(log.getOccurrence()).thenReturn(Optional.of(occurrence));
        return log;
    }

    @Test
    public void testGetLogs() throws Exception {
        /* Mock privilege */
        when(privilege.getName()).thenReturn(Privileges.Constants.VIEW_WEB_SERVICES);
        when(user.hasPrivilege("MDC", Privileges.Constants.VIEW_WEB_SERVICES)).thenReturn(true);

        /* Mock webService*/
        WebService webService = mock(WebService.class);
        when(webService.getProtocol()).thenReturn(WebServiceProtocol.SOAP);
        when(webServicesService.getWebService(anyString())).thenReturn(Optional.of(webService));

        /*Mock endPointConfiguration*/
        OutboundEndPointConfiguration ecpMock = mock(OutboundEndPointConfiguration.class);
        when(ecpMock.getLogLevel()).thenReturn(LogLevel.INFO);
        when(ecpMock.getAuthenticationMethod()).thenReturn(EndPointAuthentication.BASIC_AUTHENTICATION);
        when((ecpMock).getUsername()).thenReturn("USER");
        when((ecpMock).getPassword()).thenReturn("PASSWORD");

        WebServiceCallOccurrence occurrence = createOccurrence(
                Instant.now(),
                "Request1",
                ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName(),
                ecpMock
        );

        /* Mock logs */
        Instant time = Instant.now();

        EndPointLog log1 = mockEndPointLog(1,
                LogLevel.SEVERE,
                "message1",
                time,
                ecpMock,
                "stackTrace1",
                occurrence
        );

        EndPointLog log2 = mockEndPointLog(2,
                LogLevel.SEVERE,
                "message2",
                time,
                ecpMock,
                "stackTrace2",
                occurrence
        );

        List<EndPointLog> logs = new ArrayList<>();
        logs.add(log1);
        logs.add(log2);

        OccurrenceLogFinderBuilder builder = FakeBuilder.initBuilderStub(logs, OccurrenceLogFinderBuilder.class, Finder.class);
        when(webServiceCallOccurrenceService.getOccurrenceLogFinderBuilder()).thenReturn(builder);
        when(webServiceCallOccurrenceService.getEndPointOccurrence(1L)).thenReturn(Optional.of(occurrence));

        Response response = target("/occurrences/1/log").request().header("X-CONNEXO-APPLICATION-NAME", "MDC").get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(2);

        assertThat(jsonModel.<Integer>get("$.logs[0].id")).isEqualTo(1);
        assertThat(jsonModel.<Instant>get("$.logs[0].timestamp")).isEqualTo(time.toEpochMilli());
        assertThat(jsonModel.<String>get("$.logs[0].message")).isEqualTo("message1");
        assertThat(jsonModel.<String>get("$.logs[0].stackTrace")).isEqualTo("stackTrace1");

        assertThat(jsonModel.<Integer>get("$.logs[1].id")).isEqualTo(2);
        assertThat(jsonModel.<Instant>get("$.logs[1].timestamp")).isEqualTo(time.toEpochMilli());
        assertThat(jsonModel.<String>get("$.logs[1].message")).isEqualTo("message2");
        assertThat(jsonModel.<String>get("$.logs[1].stackTrace")).isEqualTo("stackTrace2");
    }

    @Test
    public void testRetry() throws Exception {
        /* Mock privilege */
        when(privilege.getName()).thenReturn(Privileges.Constants.RETRY_WEB_SERVICES);
        when(user.hasPrivilege("MDC", Privileges.Constants.RETRY_WEB_SERVICES)).thenReturn(true);

        /* Mock webService*/
        WebService webService = mock(WebService.class);
        when(webService.getProtocol()).thenReturn(WebServiceProtocol.SOAP);
        when(webServicesService.getWebService(anyString())).thenReturn(Optional.of(webService));

        /*Mock endPointConfiguration*/
        OutboundEndPointConfiguration ecpMock = mock(OutboundEndPointConfiguration.class);
        when(ecpMock.getLogLevel()).thenReturn(LogLevel.INFO);
        when(ecpMock.getAuthenticationMethod()).thenReturn(EndPointAuthentication.BASIC_AUTHENTICATION);
        when((ecpMock).getUsername()).thenReturn("USER");
        when((ecpMock).getPassword()).thenReturn("PASSWORD");

        WebServiceCallOccurrence occurrence = mock(WebServiceCallOccurrence.class);

        when(webServiceCallOccurrenceService.getEndPointOccurrence((long) 1)).thenReturn(Optional.of(occurrence));

        Response response = target("/occurrences/1/retry").request().header("X-CONNEXO-APPLICATION-NAME", "MDC").put(null);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(occurrence).retry();
    }

    private WebServiceCallOccurrence createOccurrence(Instant time, String request, String application, EndPointConfiguration endPointConfiguration) {
        return new WebServiceCallOccurrenceImpl(dataModel, transactionService, webServicesService)
                .init(time, request, application, endPointConfiguration);
    }
}
