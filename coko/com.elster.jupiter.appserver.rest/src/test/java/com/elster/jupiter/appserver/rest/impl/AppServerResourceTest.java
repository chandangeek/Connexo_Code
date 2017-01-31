/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.appserver.impl.ImportScheduleOnAppServerImpl;
import com.elster.jupiter.appserver.impl.SubscriberExecutionSpecImpl;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.cron.CronExpression;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppServerResourceTest extends AppServerApplicationTest {

    @Test
    public void testGetAppServers() {
        AppServer appServer = mockAppServer();
        when(appServer.isActive()).thenReturn(true);

        Response response = target("/appserver").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        AppServerInfos infos = response.readEntity(AppServerInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.appServers).hasSize(1);

        assertThat(infos.appServers.get(0).name).isEqualTo("APPSERVER");
        assertThat(infos.appServers.get(0).active).isTrue();

        assertThat(infos.appServers.get(0).executionSpecs).hasSize(1);
        assertThat(infos.appServers.get(0).executionSpecs.get(0).subscriberSpec.destination).isEqualTo("DESTINATION-SPEC-NAME");
        assertThat(infos.appServers.get(0).executionSpecs.get(0).subscriberSpec.subscriber).isEqualTo("SUBSCRIBER-SPEC-NAME");

        assertThat(infos.appServers.get(0).importServices).hasSize(1);
        assertThat(infos.appServers.get(0).importServices.get(0).id).isEqualTo(1);
        assertThat(infos.appServers.get(0).importServices.get(0).name).isEqualTo("IMPORT-SCHEDULE");
    }

    @Test
    public void testGetAppServer() {
        AppServer appServer = mockAppServer();
        when(appServer.isActive()).thenReturn(true);

        Response response = target("/appserver/APPSERVER").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        AppServerInfo info = response.readEntity(AppServerInfo.class);
        assertThat(info.name).isEqualTo("APPSERVER");
        assertThat(info.active).isTrue();
        assertThat(info.version).isEqualTo(1L);

        assertThat(info.executionSpecs).hasSize(1);
        assertThat(info.executionSpecs.get(0).subscriberSpec.destination).isEqualTo("DESTINATION-SPEC-NAME");
        assertThat(info.executionSpecs.get(0).subscriberSpec.subscriber).isEqualTo("SUBSCRIBER-SPEC-NAME");

        assertThat(info.importServices).hasSize(1);
        assertThat(info.importServices.get(0).id).isEqualTo(1);
        assertThat(info.importServices.get(0).name).isEqualTo("IMPORT-SCHEDULE");
    }

    @Test
    public void testCreateAppServer() {
        AppServer newAppServer = mock(AppServer.class);
        AppServer.BatchUpdate batchUpdate = mock(AppServer.BatchUpdate.class);
        when(newAppServer.getName()).thenReturn("NEW-APP-SERVER");
        when(newAppServer.isActive()).thenReturn(false);
        when(newAppServer.forBatchUpdate()).thenReturn(batchUpdate);
        addServicesOnAppServer(newAppServer);
        CronExpression cronExpression = mock(CronExpression.class);
        when(cronExpressionParser.parse(any(String.class))).thenReturn(Optional.of(cronExpression));
        when(appService.createAppServer(eq("NEW-APP-SERVER"), eq(cronExpression))).thenReturn(newAppServer);
        UriInfo uriInfo = mockUriInfo();
        AppServerInfo info = new AppServerInfo(newAppServer, "bla", "bla", thesaurus, webServicesService, uriInfo);
        Entity<AppServerInfo> json = Entity.json(info);

        Response response = target("/appserver").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    private UriInfo mockUriInfo() {
        UriInfo mock = mock(UriInfo.class);
        URI uri = URI.create("http://connexo/soap/");
        UriBuilder uriBuilder = FakeBuilder.initBuilderStub(uri, UriBuilder.class);
        when(mock.getBaseUriBuilder()).thenReturn(uriBuilder);
        return mock;
    }

    @Test
    public void testUpdateAppServer() {
        AppServer appServer = mockAppServer();
        UriInfo uriInfo = mockUriInfo();
        AppServerInfo info = new AppServerInfo(appServer, "bla", "bla", thesaurus, webServicesService, uriInfo);
        ImportScheduleInfo updateImportInfo = new ImportScheduleInfo();
        updateImportInfo.id = 2;
        updateImportInfo.name = "UPDATE-IMPORT";
        SubscriberExecutionSpecInfo editSubscriberExecutionInfo = new SubscriberExecutionSpecInfo();
        SubscriberSpecInfo editSubscriberInfo = new SubscriberSpecInfo();
        editSubscriberInfo.destination = "UPDATE-DESTINATION";
        editSubscriberInfo.subscriber = "UPDATE-SUBSCRIBER";
        editSubscriberExecutionInfo.subscriberSpec = editSubscriberInfo;
        info.importServices.add(updateImportInfo);
        info.executionSpecs.add(editSubscriberExecutionInfo);
        info.version = 1L;
        Entity<AppServerInfo> json = Entity.json(info);

        AppServer.BatchUpdate updater = mock(AppServer.BatchUpdate.class);
        when(appServer.forBatchUpdate()).thenReturn(updater);
        SubscriberSpec updateSubscriber = mock(SubscriberSpec.class);
        when(updateSubscriber.getName()).thenReturn("UPDATE-SUBSCRIBER");
        DestinationSpec updateDestination = mock(DestinationSpec.class);
        when(updateDestination.getName()).thenReturn("UPDATE-DESTINATION");
        when(updateSubscriber.getDestination()).thenReturn(updateDestination);
        when(messageService.getSubscriberSpec(eq("UPDATE-DESTINATION"), eq("UPDATE-SUBSCRIBER"))).thenReturn(Optional.of(updateSubscriber));

        ImportSchedule updateImport = mock(ImportSchedule.class);
        when(updateImport.getId()).thenReturn(2L);
        when(updateImport.getName()).thenReturn("UPDATE-IMPORT");
        when(fileImportService.getImportSchedule(eq(2L))).thenReturn(Optional.of(updateImport));

        Response response = target("/appserver/APPSERVER").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testRemoveAppServer() {
        AppServer appServer = mockAppServer();
        UriInfo uriInfo = mockUriInfo();
        AppServerInfo info = new AppServerInfo(appServer, "bla", "bla", thesaurus, webServicesService, uriInfo);
        Entity<AppServerInfo> json = Entity.json(info);

        Response response = target("/appserver/APPSERVER").request().build(HttpMethod.DELETE, json).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(appServer).delete();
    }

    @Test
    public void testActivateAppServer() {
        AppServer appServer = mockAppServer();
        when(appServer.isActive()).thenReturn(false);
        UriInfo uriInfo = mockUriInfo();
        AppServerInfo info = new AppServerInfo(appServer, null, null, thesaurus, webServicesService, uriInfo);
        info.active = true;
        info.exportDirectory = "c:\\export";
        info.importDirectory = "c:\\import";
        Entity<AppServerInfo> json = Entity.json(info);

        Response response = target("/appserver/APPSERVER").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(appServer.forBatchUpdate()).activate();
    }

    @Test
    public void testDeactivateAppServer() {
        AppServer appServer = mockAppServer();
        when(appServer.isActive()).thenReturn(true);
        UriInfo uriInfo = mockUriInfo();
        AppServerInfo info = new AppServerInfo(appServer, null, null, thesaurus, webServicesService, uriInfo);
        info.active = false;
        info.exportDirectory = "c:\\export";
        info.importDirectory = "c:\\import";
        Entity<AppServerInfo> json = Entity.json(info);

        Response response = target("/appserver/APPSERVER").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(appServer.forBatchUpdate()).deactivate();
    }

    @Test
    public void testGetUnservedMessageServices() {
        AppServer appServer = mockAppServer();
        SubscriberSpec unservedSubscriber = mock(SubscriberSpec.class);
        DestinationSpec unservedDestination = mock(DestinationSpec.class);
        when(messageService.getNonSystemManagedSubscribers()).thenReturn(Arrays.asList(unservedSubscriber));
        when(unservedSubscriber.getName()).thenReturn("UNSERVED-SUBSCRIBER");
        when((unservedSubscriber.getDestination())).thenReturn(unservedDestination);
        when((unservedDestination.getName())).thenReturn("UNSERVED-DESTINATION");

        Response response = target("/appserver/APPSERVER/unserved").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        SubscriberSpecInfos infos = response.readEntity(SubscriberSpecInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.subscriberSpecs).hasSize(1);
        assertThat(infos.subscriberSpecs.get(0).destination).isEqualTo("UNSERVED-DESTINATION");
        assertThat(infos.subscriberSpecs.get(0).subscriber).isEqualTo("UNSERVED-SUBSCRIBER");
    }

    @Test
    public void testGetUnservedImportServices() {
        AppServer appServer = mockAppServer();
        ImportSchedule unservedImport = mock(ImportSchedule.class);
        List<? extends ImportSchedule> unservedImports = Arrays.asList(unservedImport);
        when(unservedImport.getId()).thenReturn(2L);
        when(unservedImport.getName()).thenReturn("IMPORT-UNSERVED");
        when(fileImportService.getImportSchedules()).thenReturn(Arrays.asList(unservedImport));

        Response response = target("/appserver/APPSERVER/unservedimport").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ImportScheduleInfos infos = response.readEntity(ImportScheduleInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.importServices).hasSize(1);
        assertThat(infos.importServices.get(0).id).isEqualTo(2);
        assertThat(infos.importServices.get(0).name).isEqualTo("IMPORT-UNSERVED");
    }

    @Test
    public void testGetServedImportServices() {
        AppServer appServer = mockAppServer();
        Response response = target("/appserver/APPSERVER/servedimport").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ImportScheduleInfos infos = response.readEntity(ImportScheduleInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.importServices).hasSize(1);
        assertThat(infos.importServices.get(0).id).isEqualTo(1);
        assertThat(infos.importServices.get(0).name).isEqualTo("IMPORT-SCHEDULE");
    }

    @SuppressWarnings("unchecked")
    private AppServer mockAppServer() {
        AppServer appServer = mock(AppServer.class);
        when(appServer.getName()).thenReturn("APPSERVER");
        when(appServer.getVersion()).thenReturn(1L);
        AppServer.BatchUpdate batchUpdate = mock(AppServer.BatchUpdate.class);
        when(appServer.forBatchUpdate()).thenReturn(batchUpdate);
        addServicesOnAppServer(appServer);

        when(appService.findAppServer("APPSERVER")).thenReturn(Optional.of(appServer));
        when(appService.findAndLockAppServerByNameAndVersion("APPSERVER", 1L)).thenReturn(Optional.of(appServer));

        Query<AppServer> query = (Query<AppServer>) mock(Query.class);
        RestQuery<AppServer> restQuery = (RestQuery<AppServer>) mock(RestQuery.class);
        when(appService.getAppServerQuery()).thenReturn(query);
        when(restQueryService.wrap(query)).thenReturn(restQuery);
        when(restQuery.select(any(QueryParameters.class), any(Order.class))).thenReturn(Arrays.asList(appServer));
        when(appServer.getImportDirectory()).thenReturn(Optional.<Path>empty());
        when(dataExportService.getExportDirectory(appServer)).thenReturn(Optional.<Path>empty());

        return appServer;
    }

    @Test
    public void testCreateAppServerWithEndPoints() throws Exception {
        AppServer newAppServer = mock(AppServer.class);
        AppServer.BatchUpdate batchUpdate = mock(AppServer.BatchUpdate.class);
        when(newAppServer.getName()).thenReturn("NEW-APP-SERVER");
        when(newAppServer.isActive()).thenReturn(false);
        when(newAppServer.forBatchUpdate()).thenReturn(batchUpdate);
        addServicesOnAppServer(newAppServer);
        CronExpression cronExpression = mock(CronExpression.class);
        when(cronExpressionParser.parse(any(String.class))).thenReturn(Optional.of(cronExpression));
        when(appService.createAppServer(eq("NEW-APP-SERVER"), eq(cronExpression))).thenReturn(newAppServer);

        AppServerInfo info = new AppServerInfo();
        info.name = "NEW-APP-SERVER";
        info.exportDirectory = "X";
        info.importDirectory = "X";
        EndPointConfigurationInfo endPointConfigurationInfo = new EndPointConfigurationInfo();
        endPointConfigurationInfo.name = "cim";
        EndPointConfiguration epc1 = mock(EndPointConfiguration.class);
        when(endPointConfigurationService.getEndPointConfiguration("cim")).thenReturn(Optional.of(epc1));
        EndPointConfigurationInfo endPointConfigurationInfo2 = new EndPointConfigurationInfo();
        endPointConfigurationInfo2.name = "cim2";
        EndPointConfiguration epc2 = mock(EndPointConfiguration.class);
        when(endPointConfigurationService.getEndPointConfiguration("cim2")).thenReturn(Optional.of(epc2));
        info.endPointConfigurations = Arrays.asList(endPointConfigurationInfo, endPointConfigurationInfo2);
        Entity<AppServerInfo> json = Entity.json(info);

        Response response = target("/appserver").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(newAppServer).supportEndPoint(epc1);
        verify(newAppServer).supportEndPoint(epc2);
    }

    @Test
    public void testUpdateAppServerWithEndPoints() throws Exception {
        AppServer updatedAppServer = mock(AppServer.class);
        AppServer.BatchUpdate batchUpdate = mock(AppServer.BatchUpdate.class);
        when(updatedAppServer.getName()).thenReturn("cxo");
        when(updatedAppServer.isActive()).thenReturn(false);
        when(updatedAppServer.forBatchUpdate()).thenReturn(batchUpdate);
        when(updatedAppServer.getImportSchedulesOnAppServer()).thenReturn(Collections.emptyList());
        when(updatedAppServer.getSubscriberExecutionSpecs()).thenReturn(Collections.emptyList());
        CronExpression cronExpression = mock(CronExpression.class);
        when(cronExpressionParser.parse(any(String.class))).thenReturn(Optional.of(cronExpression));
        when(appService.findAndLockAppServerByNameAndVersion(eq("cxo"), eq(1L))).thenReturn(Optional.of(updatedAppServer));
        when(dataExportService.getExportDirectory(updatedAppServer)).thenReturn(Optional.of(Paths.get("X")));
        when(updatedAppServer.getImportDirectory()).thenReturn(Optional.of(Paths.get("X")));

        AppServerInfo info = new AppServerInfo();
        info.name = "cxo";
        info.version = 1L;
        info.exportDirectory = "X";
        info.importDirectory = "X";
        info.executionSpecs = Collections.emptyList();
        info.importServices = Collections.emptyList();
        EndPointConfigurationInfo endPointConfigurationInfo = new EndPointConfigurationInfo();
        endPointConfigurationInfo.name = "cim";
        EndPointConfiguration epc1 = mockEndpointConfiguration("cim");
        when(endPointConfigurationService.getEndPointConfiguration("cim")).thenReturn(Optional.of(epc1));
        EndPointConfigurationInfo endPointConfigurationInfo2 = new EndPointConfigurationInfo();
        endPointConfigurationInfo2.name = "cim2";
        EndPointConfiguration epc2 = mockEndpointConfiguration("cim2");
        when(endPointConfigurationService.getEndPointConfiguration("cim2")).thenReturn(Optional.of(epc2));
        EndPointConfiguration epc3 = mockEndpointConfiguration("cim3");
        when(endPointConfigurationService.getEndPointConfiguration("cim3")).thenReturn(Optional.of(epc3));
        when(updatedAppServer.supportedEndPoints()).thenReturn(Arrays.asList(epc3, epc2));
        info.endPointConfigurations = Arrays.asList(endPointConfigurationInfo, endPointConfigurationInfo2);
        Entity<AppServerInfo> json = Entity.json(info);

        Response response = target("/appserver/cxo").request().put(json);
        verify(updatedAppServer).supportEndPoint(epc1);
        verify(updatedAppServer, never()).supportEndPoint(epc2);
        verify(updatedAppServer).dropEndPointSupport(epc3);
        verify(updatedAppServer, never()).dropEndPointSupport(epc2);
    }

    @Test
    public void testGetUnusedEndpoints() throws Exception {
        AppServer appserver = mock(AppServer.class);
        AppServer.BatchUpdate batchUpdate = mock(AppServer.BatchUpdate.class);
        when(appserver.getName()).thenReturn("cxo");
        when(appserver.isActive()).thenReturn(false);
        when(appserver.forBatchUpdate()).thenReturn(batchUpdate);
        when(appserver.getImportSchedulesOnAppServer()).thenReturn(Collections.emptyList());
        when(appserver.getSubscriberExecutionSpecs()).thenReturn(Collections.emptyList());
        CronExpression cronExpression = mock(CronExpression.class);
        when(cronExpressionParser.parse(any(String.class))).thenReturn(Optional.of(cronExpression));
        when(appService.findAppServer("cxo")).thenReturn(Optional.of(appserver));
        when(dataExportService.getExportDirectory(appserver)).thenReturn(Optional.of(Paths.get("X")));
        when(appserver.getImportDirectory()).thenReturn(Optional.of(Paths.get("X")));
        EndPointConfiguration epc = mockEndpointConfiguration("cim");
        when(epc.isInbound()).thenReturn(true);
        EndPointConfiguration epc2 = mockEndpointConfiguration("cim2");
        when(epc2.isInbound()).thenReturn(true);
        List<EndPointConfiguration> list = new ArrayList<>();
        list.add(epc);
        list.add(epc2);
        Finder<EndPointConfiguration> finder = mockFinder(list);
        when(appserver.supportedEndPoints()).thenReturn(Collections.singletonList(epc));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);
        WebService webService = mock(WebService.class);
        when(webService.getProtocol()).thenReturn(WebServiceProtocol.REST);
        when(webServicesService.getWebService(any())).thenReturn(Optional.of(webService));
        Response response = target("/appserver/cxo/unusedendpoints").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());

        assertThat(jsonModel.<String>get("[0].name")).isEqualTo("cim2");
        assertThat(jsonModel.hasPath("[1]")).isFalse();
    }

    private EndPointConfiguration mockEndpointConfiguration(String endpointConfigurationName) {
        InboundEndPointConfiguration endPointConfiguration = mock(InboundEndPointConfiguration.class);
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(1L);
        when(group.getName()).thenReturn("Name");
        when(endPointConfiguration.getGroup()).thenReturn(Optional.of(group));
        when(endPointConfiguration.getName()).thenReturn(endpointConfigurationName);
        when(endPointConfiguration.getLogLevel()).thenReturn(LogLevel.CONFIG);
        EndPointAuthentication endPointAuthentication = EndPointAuthentication.NONE;
        when(endPointConfiguration.getAuthenticationMethod()).thenReturn(endPointAuthentication);

        return endPointConfiguration;
    }

    @SuppressWarnings("unchecked")
    private void addServicesOnAppServer(AppServer mocked) {
        SubscriberSpec subscriberSpec = mock(SubscriberSpec.class);
        SubscriberSpec updateSubscriberSpec = mock(SubscriberSpec.class);
        SubscriberExecutionSpecImpl subscriberExecutionSpec = mock(SubscriberExecutionSpecImpl.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        List<? extends SubscriberExecutionSpec> subscribers = Arrays.asList(subscriberExecutionSpec);
        when(subscriberExecutionSpec.getSubscriberSpec()).thenReturn(subscriberSpec);
        when(subscriberSpec.getName()).thenReturn("SUBSCRIBER-SPEC-NAME");
        when((subscriberSpec.getDestination())).thenReturn(destinationSpec);
        when((destinationSpec.getName())).thenReturn("DESTINATION-SPEC-NAME");
        when(messageService.getSubscriberSpec(eq("DESTINATION-SPEC-NAME"), eq("SUBSCRIBER-SPEC-NAME"))).thenReturn(Optional.of(subscriberSpec));
        doReturn(subscribers).when(mocked).getSubscriberExecutionSpecs();

        ImportSchedule importSchedule = mock(ImportSchedule.class);
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = mock(ImportScheduleOnAppServerImpl.class);
        List<? extends ImportScheduleOnAppServer> importSchedules = Arrays.asList(importScheduleOnAppServer);
        when(importScheduleOnAppServer.getImportSchedule()).thenReturn(Optional.of(importSchedule));
        when(importSchedule.getId()).thenReturn(1L);
        when(importSchedule.getName()).thenReturn("IMPORT-SCHEDULE");
        when(fileImportService.getImportSchedule(eq(1L))).thenReturn(Optional.of(importSchedule));
        doReturn(importSchedules).when(mocked).getImportSchedulesOnAppServer();
    }

    @SuppressWarnings("unchecked")
    private void mockTransaction() {
        when(transactionService.<Object>execute(Matchers.any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                @SuppressWarnings("rawtypes")
                Transaction transaction = (Transaction) invocation.getArguments()[0];
                return transaction.perform();
            }
        });
    }

    @Test
    public void testDeleteAppServerConflict() {
        when(appService.findAndLockAppServerByNameAndVersion("appserverName", 7)).thenReturn(Optional.empty());
        when(appService.findAppServer("appserverName")).thenReturn(Optional.empty());

        AppServerInfo info = new AppServerInfo();
        info.name = "appserverName";
        info.version = 7;
        Response response = target("appserver/appserverName").request().method("DELETE", Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testUpdateAppServerConflict() {
        when(appService.findAndLockAppServerByNameAndVersion("appserverName", 7)).thenReturn(Optional.empty());
        when(appService.findAppServer("appserverName")).thenReturn(Optional.empty());

        AppServer appServer = mockAppServer();
        UriInfo uriInfo = mockUriInfo();
        AppServerInfo info = new AppServerInfo(appServer, "bla", "bla", thesaurus, webServicesService, uriInfo);
        info.name = "appserverName";
        info.version = 7;
        Response response = target("appserver/appserverName").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testCreateAppServerInvalidExportDir() {
        when(fileSystem.getPath("invalid")).thenThrow(new InvalidPathException("", ""));
        AppServerInfo info = new AppServerInfo();
        info.exportDirectory = "invalid";
        info.importDirectory = "valid";

        Response response = target("/appserver").request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateAppServerInvalidImportDir() {
        when(fileSystem.getPath("invalid")).thenThrow(new InvalidPathException("", ""));
        AppServerInfo info = new AppServerInfo();
        info.exportDirectory = "valid";
        info.importDirectory = "invalid";

        Response response = target("/appserver").request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(com.elster.jupiter.domain.util.QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }
}
