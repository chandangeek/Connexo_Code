package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.appserver.impl.ImportScheduleOnAppServerImpl;
import com.elster.jupiter.appserver.impl.SubscriberExecutionSpecImpl;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.cron.CronExpression;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AppServerResourceTest extends AppServerApplicationTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

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
        AppServer.BatchUpdate batchUpdate = mock (AppServer.BatchUpdate.class);
        when(newAppServer.getName()).thenReturn("NEW-APP-SERVER");
        when(newAppServer.isActive()).thenReturn(false);
        when(newAppServer.forBatchUpdate()).thenReturn(batchUpdate);
        addServicesOnAppServer(newAppServer);
        CronExpression cronExpression = mock(CronExpression.class);
        when(cronExpressionParser.parse(any(String.class))).thenReturn(Optional.of(cronExpression));
        when(appService.createAppServer(eq("NEW-APP-SERVER"), eq(cronExpression))).thenReturn(newAppServer);

        AppServerInfo info = new AppServerInfo(newAppServer, thesaurus);
        Entity<AppServerInfo> json = Entity.json(info);

        Response response = target("/appserver").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testUpdateAppServer() {
        AppServer appServer = mockAppServer();
        AppServerInfo info = new AppServerInfo(appServer, thesaurus);
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
        Response response = target("/appserver/APPSERVER").request().delete();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(appServer).delete();
    }

    @Test
    public void testActivateAppServer() {
        AppServer appServer = mockAppServer();
        when(appServer.isActive()).thenReturn(false);

        Response response = target("/appserver/APPSERVER/activate").request().put(Entity.json(null));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(appServer).activate();
    }

    @Test
    public void testDeactivateAppServer() {
        AppServer appServer = mockAppServer();
        when(appServer.isActive()).thenReturn(true);

        Response response = target("/appserver/APPSERVER/deactivate").request().put(Entity.json(null));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(appServer).deactivate();
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
        addServicesOnAppServer(appServer);

        when(appService.findAppServer("APPSERVER")).thenReturn(Optional.of(appServer));

        Query<AppServer> query = (Query<AppServer>) mock(Query.class);
        RestQuery<AppServer> restQuery = (RestQuery<AppServer>) mock(RestQuery.class);
        when(appService.getAppServerQuery()).thenReturn(query);
        when(restQueryService.wrap(query)).thenReturn(restQuery);
        when(restQuery.select(any(QueryParameters.class), any(Order.class))).thenReturn(Arrays.asList(appServer));

        return appServer;
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
}
