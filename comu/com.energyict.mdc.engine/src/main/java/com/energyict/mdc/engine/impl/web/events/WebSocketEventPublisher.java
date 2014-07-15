package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.EventReceiver;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.web.events.commands.Request;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParseException;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParser;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import org.eclipse.jetty.websocket.WebSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link EventPublisher} interface
 * that will publish events through a WebSocket to the registered client application.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (17:17)
 */
public class WebSocketEventPublisher implements EventReceiver, EventPublisher, WebSocket.OnTextMessage {

    private final ServiceProvider serviceProvider;
    private final WebSocketCloseEventListener closeEventListener;
    private EventPublisher systemWideEventPublisher;
    private Connection connection;
    private RequestParser parser;
    private boolean sendBinary = false;

    public WebSocketEventPublisher(ServiceProvider serviceProvider, WebSocketCloseEventListener closeEventListener) {
        this(serviceProvider, EventPublisherImpl.getInstance(), closeEventListener);
    }

    public WebSocketEventPublisher(ServiceProvider serviceProvider, EventPublisher systemWideEventPublisher, WebSocketCloseEventListener closeEventListener) {
        super();
        this.serviceProvider = serviceProvider;
        this.closeEventListener = closeEventListener;
        this.systemWideEventPublisher = systemWideEventPublisher;
        this.parser = new RequestParser(serviceProvider);
    }

    @Override
    public void receive (ComServerEvent event) {
        this.publish(event);
    }

    @Override
    public void publish (ComServerEvent event) {
        this.sendEvent(event);
    }

    private void sendMessage (String message) {
        try {
            if (this.isConnected()) {
                if (this.sendBinary) {
                    this.sendBinary(message);
                }
                else {
                    this.connection.sendMessage(message);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void sendEvent (ComServerEvent event) {
        try {
            if (this.isConnected()) {
                if (this.sendBinary) {
                    this.sendBinary(event);
                }
                else {
                    this.connection.sendMessage(event.toString());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void sendBinary (Serializable object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            byte[] bytes = baos.toByteArray();
            this.connection.sendMessage(bytes, 0, bytes.length);
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private boolean isConnected () {
        return this.connection != null;
    }

    @Override
    public void onMessage (String message) {
        try {
            Request request = this.parser.parse(message);
            request.applyTo(this);
            this.sendBinary = request.useBinaryEvents();
            this.sendMessage("Copy " + message);
        }
        catch (RequestParseException e) {
            this.sendMessage("Message not understood:" + e.getMessage());
        }
    }

    @Override
    public void onOpen (Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onClose (int closeCode, String message) {
        this.connection = null;
        this.systemWideEventPublisher.unregisterAllInterests(this);
        this.closeEventListener.closedFrom(this);
    }

    @Override
    public void unregisterAllInterests (EventReceiver receiver) {
        // Unregistering only happens via onClose
    }

    @Override
    public void registerInterest (EventReceiver receiver) {
        this.systemWideEventPublisher.registerInterest(this);
    }

    @Override
    public void narrowInterestToCategories (EventReceiver receiver, Set<Category> categories) {
        this.systemWideEventPublisher.narrowInterestToCategories(this, categories);
    }

    @Override
    public void narrowInterestToDevices (EventReceiver receiver, List<BaseDevice> devices) {
        this.systemWideEventPublisher.narrowInterestToDevices(this, devices);
    }

    @Override
    public void widenInterestToAllDevices (EventReceiver receiver) {
        this.systemWideEventPublisher.widenInterestToAllDevices(this);
    }

    @Override
    public void narrowInterestToConnectionTasks (EventReceiver receiver, List<ConnectionTask> connectionTasks) {
        this.systemWideEventPublisher.narrowInterestToConnectionTasks(this, connectionTasks);
    }

    @Override
    public void widenInterestToAllConnectionTasks (EventReceiver receiver) {
        this.systemWideEventPublisher.widenInterestToAllConnectionTasks(this);
    }

    @Override
    public void narrowInterestToComTaskExecutions (EventReceiver receiver, List<ComTaskExecution> comTaskExecutions) {
        this.systemWideEventPublisher.narrowInterestToComTaskExecutions(this, comTaskExecutions);
    }

    @Override
    public void widenInterestToAllComTaskExecutions (EventReceiver receiver) {
        this.systemWideEventPublisher.widenInterestToAllComTaskExecutions(this);
    }

    @Override
    public void narrowInterestToComPorts (EventReceiver receiver, List<ComPort> comPorts) {
        this.systemWideEventPublisher.narrowInterestToComPorts(this, comPorts);
    }

    @Override
    public void widenInterestToAllComPorts (EventReceiver receiver) {
        this.systemWideEventPublisher.widenInterestToAllComPorts(this);
    }

    @Override
    public void narrowInterestToComPortPools (EventReceiver receiver, List<ComPortPool> comPortPools) {
        this.systemWideEventPublisher.narrowInterestToComPortPools(this, comPortPools);
    }

    @Override
    public void widenInterestToAllComPortPools (EventReceiver receiver) {
        this.systemWideEventPublisher.widenInterestToAllComPortPools(this);
    }

    @Override
    public void narrowInterestToLogLevel (EventReceiver receiver, LogLevel logLevel) {
        this.systemWideEventPublisher.narrowInterestToLogLevel(this, logLevel);
    }

    @Override
    public void widenToAllLogLevels (EventReceiver receiver) {
        this.systemWideEventPublisher.widenToAllLogLevels(this);
    }

}