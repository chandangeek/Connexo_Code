package com.energyict.mdc.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OfflineComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.rest.impl.comserver.InboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.OfflineComServerInfo;
import com.energyict.mdc.rest.impl.comserver.OnlineComServerInfo;
import com.energyict.mdc.rest.impl.comserver.RemoteComServerInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * When accessing a resource, I choose not to use UriBuilder, as you should be aware that changing the URI means changing the API!
 * Hard coding URLS here will be a "gently" reminder
 * @author bvn
 */
public class ComServerResourceTest extends ComserverCoreApplicationJerseyTest {


    @Test
    public void testGetNonExistingComServer() throws Exception {
        when(engineConfigurationService.findComServer(anyInt())).thenReturn(Optional.empty());
        final Response response = target("/comservers/8").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetComPortsForNonExistingComServer() throws Exception {
        when(engineConfigurationService.findComServer(anyInt())).thenReturn(Optional.empty());
        final Response response = target("/comservers/8/comports").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetExistingComServerJSStyle() {
        OnlineComServer mock = mock(OnlineComServer.class);
        List<ComServer> comServers = new ArrayList<>();
        comServers.add(mock);
        Finder<ComServer> finder = mock(Finder.class);
        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), anyBoolean())).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(comServers);
        when(engineConfigurationService.findAllComServers()).thenReturn(finder);
        when(mock.getName()).thenReturn("Test");
        when(mock.getQueryApiPostUri()).thenReturn("/test");
        when(mock.getId()).thenReturn(1L);
        when(mock.getEventRegistrationUri()).thenReturn("/event/registration/uri");
        when(mock.getNumberOfStoreTaskThreads()).thenReturn(2);
        when(mock.getStoreTaskQueueSize()).thenReturn(3);
        when(mock.getStoreTaskThreadPriority()).thenReturn(4);
        when(mock.getChangesInterPollDelay()).thenReturn(new TimeDuration("6 seconds"));
        when(mock.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.ERROR);
        when(mock.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(mock.getSchedulingInterPollDelay()).thenReturn(new TimeDuration("7 minutes"));

        final Map<String, Object> response = target("/comservers").request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).describedAs("Should contain field 'data'").containsKey("data").containsKey("total").hasSize(2);
        List<Map<String, Object>> comServers1 = (List<Map<String, Object>>) response.get("data");
        assertThat(comServers1).describedAs("Expected only 1 comServer").hasSize(1);
        Map<String, Object> comServer1 = comServers1.get(0);
        assertThat(comServer1)
                .contains(MapEntry.entry("id", 1))
                .contains(MapEntry.entry("name", "Test"))
                .contains(MapEntry.entry("queryAPIPostUri", "/test"))
                .contains(MapEntry.entry("eventRegistrationUri", "/event/registration/uri"))
                .contains(MapEntry.entry("numberOfStoreTaskThreads", 2))
                .contains(MapEntry.entry("storeTaskQueueSize", 3))
                .contains(MapEntry.entry("storeTaskThreadPriority", 4))
                .containsKey("changesInterPollDelay")
                .containsKey("schedulingInterPollDelay")
                .contains(MapEntry.entry("communicationLogLevel", "Error"))
                .contains(MapEntry.entry("serverLogLevel", "Information"));

        Map<String, Object> changesInterPollDelay = (Map<String, Object>) comServer1.get("changesInterPollDelay");
        assertThat(changesInterPollDelay).hasSize(2)
                .contains(MapEntry.entry("count", 6))
                .contains(MapEntry.entry("timeUnit", "seconds"));
        Map<String, Object> schedulingInterPollDelay = (Map<String, Object>) comServer1.get("schedulingInterPollDelay");
        assertThat(schedulingInterPollDelay).hasSize(2)
                .contains(MapEntry.entry("count", 7))
                .contains(MapEntry.entry("timeUnit", "minutes"));
    }

    @Test
    public void testObjectMapperSerializesTypeInformation() throws Exception {
        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new";
        ObjectMapper objectMapper = new ObjectMapper();
        String response = objectMapper.writeValueAsString(onlineComServerInfo);
        assertThat(response).contains("\"comServerType\":\"Online\"");
    }

    @Test
    public void testUpdateExistingComServer() throws Exception {
        long comServer_id = 3;

        OnlineComServer serverSideComServer = mock(OnlineComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServer_id);
        when(engineConfigurationService.findComServer(comServer_id)).thenReturn(Optional.<ComServer>of(serverSideComServer));

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.eventRegistrationUri="/new/uri";
        onlineComServerInfo.schedulingInterPollDelay=new TimeDurationInfo();
        onlineComServerInfo.schedulingInterPollDelay.timeUnit="seconds";
        onlineComServerInfo.schedulingInterPollDelay.count=6;
        onlineComServerInfo.communicationLogLevel= ComServer.LogLevel.ERROR;
        onlineComServerInfo.serverLogLevel= ComServer.LogLevel.DEBUG;
        onlineComServerInfo.inboundComPorts = new ArrayList<>();
        onlineComServerInfo.outboundComPorts = new ArrayList<>();
        onlineComServerInfo.storeTaskQueueSize= 7;
        onlineComServerInfo.storeTaskThreadPriority= 8;
        onlineComServerInfo.numberOfStoreTaskThreads= 9;
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);

        final Response response = target("/comservers/3").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(serverSideComServer).save();
        ArgumentCaptor<String> onlineComServerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(serverSideComServer).setName(onlineComServerArgumentCaptor.capture());
        assertThat(onlineComServerArgumentCaptor.getValue()).isEqualTo("new name");

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(serverSideComServer).setEventRegistrationUri(uriCaptor.capture());
        assertThat(uriCaptor.getValue()).isEqualTo("/new/uri");

        assertThat(serverSideComServer.usesDefaultEventRegistrationUri()).isEqualTo(false);

        ArgumentCaptor<TimeDuration> timeDurationCaptor = ArgumentCaptor.forClass(TimeDuration.class);
        verify(serverSideComServer).setSchedulingInterPollDelay(timeDurationCaptor.capture());
        assertThat(timeDurationCaptor.getValue()).isEqualTo(new TimeDuration(6));

        ArgumentCaptor<ComServer.LogLevel> logLevelCaptor = ArgumentCaptor.forClass(ComServer.LogLevel.class);
        verify(serverSideComServer).setCommunicationLogLevel(logLevelCaptor.capture());
        assertThat(logLevelCaptor.getValue()).isEqualTo(ComServer.LogLevel.ERROR);

        ArgumentCaptor<ComServer.LogLevel> serverLogLevelCaptor = ArgumentCaptor.forClass(ComServer.LogLevel.class);
        verify(serverSideComServer).setServerLogLevel(serverLogLevelCaptor.capture());
        assertThat(serverLogLevelCaptor.getValue()).isEqualTo(ComServer.LogLevel.DEBUG);

        ArgumentCaptor<Integer> storeTaskQueueCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(serverSideComServer).setStoreTaskQueueSize(storeTaskQueueCaptor.capture());
        assertThat(storeTaskQueueCaptor.getValue()).isEqualTo(7);

        ArgumentCaptor<Integer> storeTaskThreadCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(serverSideComServer).setStoreTaskThreadPriority(storeTaskThreadCaptor.capture());
        assertThat(storeTaskThreadCaptor.getValue()).isEqualTo(8);

        ArgumentCaptor<Integer> numberOfStoreTaskThreadCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(serverSideComServer).setNumberOfStoreTaskThreads(numberOfStoreTaskThreadCaptor.capture());
        assertThat(numberOfStoreTaskThreadCaptor.getValue()).isEqualTo(9);
    }

    @Test
    public void createOnlineComServer() throws Exception {
        long comServer_id = 3;

        OnlineComServer serverSideComServer = mock(OnlineComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServer_id);
        when(engineConfigurationService.newOnlineComServerInstance()).thenReturn(serverSideComServer);

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        TimeDurationInfo timeDurationInfo = new TimeDurationInfo();
        timeDurationInfo.count=2;
        timeDurationInfo.timeUnit="seconds";
        List<InboundComPortInfo> inboundPorts = new ArrayList<>();
        onlineComServerInfo.inboundComPorts =inboundPorts;
        onlineComServerInfo.outboundComPorts =new ArrayList<>();

        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);

        final Response response = target("/comservers").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(serverSideComServer).save();
    }

    @Test
    public void createOfflineComServer() throws Exception {
        long comServer_id = 3;

        OfflineComServer serverSideComServer = mock(OfflineComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServer_id);
        when(engineConfigurationService.newOfflineComServerInstance()).thenReturn(serverSideComServer);

        OfflineComServerInfo offlineComServerInfo = new OfflineComServerInfo();
        offlineComServerInfo.name="new name";
        TimeDurationInfo timeDurationInfo = new TimeDurationInfo();
        timeDurationInfo.count=2;
        timeDurationInfo.timeUnit="seconds";
        offlineComServerInfo.inboundComPorts = new ArrayList<>();
        offlineComServerInfo.outboundComPorts =new ArrayList<>();

        Entity<OfflineComServerInfo> json = Entity.json(offlineComServerInfo);

        final Response response = target("/comservers").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(serverSideComServer).save();
    }

    @Test
    public void createRemoteComServer() throws Exception {
        long comServer_id = 3;
        long onlineComServerId = 5;

        RemoteComServer serverSideComServer = mock(RemoteComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServer_id);
        when(engineConfigurationService.newRemoteComServerInstance()).thenReturn(serverSideComServer);

        OnlineComServer onlineComServer = mock(OnlineComServer.class);
        when(engineConfigurationService.findComServer(onlineComServerId)).thenReturn(Optional.<ComServer>of(onlineComServer));

        RemoteComServerInfo remoteComServerInfo = new RemoteComServerInfo();
        remoteComServerInfo.name="new name";
        remoteComServerInfo.onlineComServerId = 5L;
        TimeDurationInfo timeDurationInfo = new TimeDurationInfo();
        timeDurationInfo.count=2;
        timeDurationInfo.timeUnit="seconds";
        remoteComServerInfo.inboundComPorts = new ArrayList<>();
        remoteComServerInfo.outboundComPorts =new ArrayList<>();

        Entity<RemoteComServerInfo> json = Entity.json(remoteComServerInfo);

        final Response response = target("/comservers").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(serverSideComServer).save();
    }

    @Test
    public void testCannotUpdateNonExistingComServer() throws Exception {
        when(engineConfigurationService.findComServer(anyInt())).thenReturn(Optional.empty());

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.inboundComPorts = new ArrayList<>();
        onlineComServerInfo.outboundComPorts = new ArrayList<>();
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        final Response response = target("/comservers/3").request().put(json); //5 was mocked, there is no ComServer 3

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteComServer() throws Exception {
        int comServer_id = 5;

        ComServer serverSideComServer = mock(OnlineComServer.class);
        when(engineConfigurationService.findComServer(comServer_id)).thenReturn(Optional.of(serverSideComServer));

        final Response response = target("/comservers/5").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(serverSideComServer).makeObsolete();
    }

    @Test
    public void testDeleteNonExistingComServerThrows404() throws Exception {
        when(engineConfigurationService.findComServer(anyInt())).thenReturn(Optional.empty());
        final Response response = target("/comservers/5").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testCreateComServerThrowsConstraintViolationException() throws Exception {
        long comServer_id = 3;

        OnlineComServer serverSideComServer = mock(OnlineComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServer_id);
        when(engineConfigurationService.newOnlineComServerInstance()).thenReturn(serverSideComServer);
        Set<ConstraintViolation<?>> constrainViolations = new HashSet<>();

        ConstraintViolation constraintViolation1 = mock(ConstraintViolation.class);
        when(constraintViolation1.getMessageTemplate()).thenReturn("{MDC.CanNotBeNull}");
        when(constraintViolation1.getPropertyPath()).thenReturn(mock(Path.class));
        constrainViolations.add(constraintViolation1);

        ConstraintViolation constraintViolation2 = mock(ConstraintViolation.class);
        when(constraintViolation2.getMessageTemplate()).thenReturn("{MDC.CanNotBeNull}");
        when(constraintViolation2.getPropertyPath()).thenReturn(mock(Path.class));
        constrainViolations.add(constraintViolation2);

        when(nlsService.interpolate(Matchers.<ConstraintViolation<?>>anyObject())).thenReturn("Property can not be null");
        ConstraintViolationException toBeThrown = new ConstraintViolationException(constrainViolations);
        doThrow(toBeThrown).when(serverSideComServer).save();
        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        TimeDurationInfo timeDurationInfo = new TimeDurationInfo();
        timeDurationInfo.count=2;
        timeDurationInfo.timeUnit="seconds";
        onlineComServerInfo.inboundComPorts = new ArrayList<>();
        onlineComServerInfo.outboundComPorts =new ArrayList<>();

        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);

        final Response response = target("/comservers").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetComServersPaged() throws Exception {
        Finder<ComServer> finder = mock(Finder.class);
        when(engineConfigurationService.findAllComServers()).thenReturn(finder);
        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), anyBoolean())).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(Collections.<ComServer>emptyList());

        final Response response = target("/comservers/").queryParam("start", 10).queryParam("limit", 20).queryParam("sort", "name").queryParam("dir", "ASC").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<JsonQueryParameters> argumentCaptor = ArgumentCaptor.forClass(JsonQueryParameters.class);
        verify(finder).from(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isNotNull();
    }
}