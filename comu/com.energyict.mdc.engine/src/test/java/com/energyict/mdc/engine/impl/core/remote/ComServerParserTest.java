package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.impl.core.remote.ComServerParser;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

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
    public void testOnline () throws JSONException {
        // Business method
        ComServer comServer = new ComServerParser().parse(new JSONObject(ONLINE_COMSERVER_AS_QUERY_RESULT));

        // Asserts
        Assertions.assertThat(comServer).isInstanceOf(OnlineComServer.class);
        OnlineComServer onlineComServer = (OnlineComServer) comServer;
        Assertions.assertThat(onlineComServer.getName()).isEqualTo("online.comserver.energyict.com");
        Assertions.assertThat(onlineComServer.isActive()).isTrue();
        Assertions.assertThat(onlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        Assertions.assertThat(onlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        Assertions.assertThat(onlineComServer.getChangesInterPollDelay()).isEqualTo(TimeDuration.seconds(18000));
        Assertions.assertThat(onlineComServer.getSchedulingInterPollDelay()).isEqualTo(TimeDuration.seconds(60));
        Assertions.assertThat(onlineComServer.getEventRegistrationUri()).isEqualTo("ws://online.comserver.energyict.com/events/registration");
        Assertions.assertThat(onlineComServer.getQueryApiPostUri()).isNull();
        Assertions.assertThat(onlineComServer.getStoreTaskQueueSize()).isEqualTo(50);
        Assertions.assertThat(onlineComServer.getNumberOfStoreTaskThreads()).isEqualTo(1);
        Assertions.assertThat(onlineComServer.getStoreTaskThreadPriority()).isEqualTo(5);
    }

    @Test
    public void testRemote () throws JSONException {
        // Business method
        ComServer comServer = new ComServerParser().parse(new JSONObject(REMOTE_COMSERVER_AS_QUERY_RESULT));

        // Asserts
        Assertions.assertThat(comServer).isInstanceOf(RemoteComServer.class);
        RemoteComServer remoteComServer = (RemoteComServer) comServer;
        Assertions.assertThat(remoteComServer.getName()).isEqualTo("remote.comserver.energyict.com");
        Assertions.assertThat(remoteComServer.isActive()).isTrue();
        Assertions.assertThat(remoteComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        Assertions.assertThat(remoteComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        Assertions.assertThat(remoteComServer.getChangesInterPollDelay()).isEqualTo(TimeDuration.seconds(1800));
        Assertions.assertThat(remoteComServer.getSchedulingInterPollDelay()).isEqualTo(TimeDuration.seconds(600));
        Assertions.assertThat(remoteComServer.getEventRegistrationUri()).isEqualTo("ws://remote.comserver.energyict.com/events/registration");
    }

}