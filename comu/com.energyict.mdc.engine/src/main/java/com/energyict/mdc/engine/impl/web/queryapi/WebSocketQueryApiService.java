package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.core.remote.QueryMethod;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import org.eclipse.jetty.websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Services clients of the remote query API.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-09 (11:11)
 */
public class WebSocketQueryApiService implements WebSocket.OnTextMessage {

    private RunningOnlineComServer comServer;
    private Connection connection;

    public WebSocketQueryApiService (RunningOnlineComServer comServer) {
        super();
        this.comServer = comServer;
    }

    public OnlineComServer getComServer () {
        return this.comServer.getComServer();
    }

    @Override
    public void onMessage (String data) {
        try {
            JSONObject jsonQuery = new JSONObject(data);
            String queryMethodName = (String) jsonQuery.get(RemoteComServerQueryJSonPropertyNames.METHOD);
            QueryMethod queryMethod = QueryMethod.byName(queryMethodName);
            String result = this.execute(queryMethod, jsonQuery);
            this.sendMessage(result);
        }
        catch (IOException e) {
            this.sendMessage("Failure to marchall result:" + e.getMessage());
        }
        catch (DataAccessException e) {
            this.sendMessage("Unexpected failure:" + e.getMessage());
        }
        catch (JSONException e) {
            this.sendMessage("Message not understood:" + e.getMessage());
        }
    }

    private String execute (QueryMethod queryMethod, JSONObject jsonQuery) throws JSONException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            String result = this.doExecute(queryMethod, jsonQuery);
            this.comServer.queryApiCallCompleted(System.currentTimeMillis() - startTime);
            return result;
        }
        catch (JSONException | RuntimeException | IOException e) {
            this.comServer.queryApiCallFailed(System.currentTimeMillis() - startTime);
            throw e;
        }
    }

    private String doExecute (QueryMethod queryMethod, JSONObject jsonQuery) throws JSONException, IOException {
        Map<String, Object> parameters = this.extractQueryParameters(jsonQuery);
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(introspector);
        mapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        mapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        Object queryResultValue = queryMethod.execute(parameters, new ComServerDAOImpl(ServiceProvider.instance.get()));
        mapper.writeValue(
                writer,
                QueryResult.forResult(
                        jsonQuery.getString(RemoteComServerQueryJSonPropertyNames.QUERY_ID),
                        queryResultValue));
        return writer.toString();
    }

    private Map<String, Object> extractQueryParameters (JSONObject jsonQuery) throws JSONException {
        Map<String, Object> parameters = new HashMap<>();
        Iterator keys = jsonQuery.keys();
        while (keys.hasNext()) {
            Object next = keys.next();
            String key = (String) next;
            if (this.isQueryParameter(key)) {
                parameters.put(key, jsonQuery.get(key));
            }
        }
        return parameters;
    }

    private boolean isQueryParameter (String key) {
        return !(   RemoteComServerQueryJSonPropertyNames.QUERY_ID.equals(key)
                 || RemoteComServerQueryJSonPropertyNames.METHOD.equals(key));
    }

    @Override
    public void onOpen (Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onClose (int closeCode, String message) {
        this.connection = null;
        this.comServer.queryApiClientUnregistered();
    }

    private void sendMessage (String message) {
        try {
            if (this.isConnected()) {
                this.connection.sendMessage(message);
            }
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private boolean isConnected () {
        return this.connection != null;
    }

}