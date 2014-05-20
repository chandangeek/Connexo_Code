package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.engine.model.impl.EngineModelServiceImpl;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.remote.ComServerParser} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-11 (09:56)
 */
public class ComServerParserTest {

    private static final String ONLINE_COMSERVER_AS_QUERY_RESULT = "{\"query-id\":\"testGetThisComServer\",\"single-value\":{\"name\":\"online.comserver.energyict.com\",\"type\":\"OnlineComServerImpl\",\"active\":true,\"serverLogLevel\":\"ERROR\",\"communicationLogLevel\":\"DEBUG\",\"changesInterPollDelay\":{\"seconds\":18000},\"schedulingInterPollDelay\":{\"seconds\":60},\"eventRegistrationUri\":\"ws://online.comserver.energyict.com/events/registration\",\"storeTaskQueueSize\":50,\"numberOfStoreTaskThreads\":1,\"storeTaskThreadPriority\":5}}";
    private static final String REMOTE_COMSERVER_AS_QUERY_RESULT = "{\"query-id\":\"testGetThisComServer\",\"single-value\":{\"name\":\"remote.comserver.energyict.com\",\"active\":true,\"serverLogLevel\":\"DEBUG\",\"communicationLogLevel\":\"ERROR\",\"changesInterPollDelay\":{\"seconds\":1800},\"schedulingInterPollDelay\":{\"seconds\":600},\"eventRegistrationUri\":\"ws://remote.comserver.energyict.com/events/registration\",\"type\":\"RemoteComServerImpl\"}}";

    @Test
    public void testDelegateToEngineModelService () throws JSONException {
        // Business method
        EngineModelService engineModelService = mock(EngineModelService.class);
        JSONObject jsonObject = new JSONObject(ONLINE_COMSERVER_AS_QUERY_RESULT);
        ComServer comServer = new ComServerParser(engineModelService).parse(jsonObject);

        // Asserts
        verify(engineModelService).parseComServerQueryResult(jsonObject);
    }

    @Test
    public void testOnline () throws JSONException {
        // Business method
        EngineModelServiceImpl engineModelService = new EngineModelServiceImpl(mock(OrmService.class), mock(NlsService.class), mock(ProtocolPluggableService.class));
        ComServer comServer = new ComServerParser(engineModelService).parse(new JSONObject(ONLINE_COMSERVER_AS_QUERY_RESULT));

        // Asserts
        assertThat(comServer).isInstanceOf(OnlineComServer.class);
        OnlineComServer onlineComServer = (OnlineComServer) comServer;
        assertThat(onlineComServer.getName()).isEqualTo("online.comserver.energyict.com");
        assertThat(onlineComServer.isActive()).isTrue();
        assertThat(onlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(onlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(onlineComServer.getChangesInterPollDelay()).isEqualTo(TimeDuration.seconds(18000));
        assertThat(onlineComServer.getSchedulingInterPollDelay()).isEqualTo(TimeDuration.seconds(60));
        assertThat(onlineComServer.getEventRegistrationUri()).isEqualTo("ws://online.comserver.energyict.com/events/registration");
        assertThat(onlineComServer.getQueryApiPostUri()).isNull();
        assertThat(onlineComServer.getStoreTaskQueueSize()).isEqualTo(50);
        assertThat(onlineComServer.getNumberOfStoreTaskThreads()).isEqualTo(1);
        assertThat(onlineComServer.getStoreTaskThreadPriority()).isEqualTo(5);
    }

    @Test
    public void testRemote () throws JSONException {
        EngineModelServiceImpl engineModelService = new EngineModelServiceImpl(mock(OrmService.class), mock(NlsService.class), mock(ProtocolPluggableService.class));

        // Business method
        ComServer comServer = new ComServerParser(engineModelService).parse(new JSONObject(REMOTE_COMSERVER_AS_QUERY_RESULT));

        // Asserts
        assertThat(comServer).isInstanceOf(RemoteComServer.class);
        RemoteComServer remoteComServer = (RemoteComServer) comServer;
        assertThat(remoteComServer.getName()).isEqualTo("remote.comserver.energyict.com");
        assertThat(remoteComServer.isActive()).isTrue();
        assertThat(remoteComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(remoteComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(remoteComServer.getChangesInterPollDelay()).isEqualTo(TimeDuration.seconds(1800));
        assertThat(remoteComServer.getSchedulingInterPollDelay()).isEqualTo(TimeDuration.seconds(600));
        assertThat(remoteComServer.getEventRegistrationUri()).isEqualTo("ws://remote.comserver.energyict.com/events/registration");
    }

}