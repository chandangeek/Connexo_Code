/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventReceiver;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.web.events.commands.Request;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParseException;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParser;

import org.eclipse.jetty.websocket.WebSocket;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link EventPublisher} interface
 * that will publish events through a WebSocket to the registered client application.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (17:17)
 */
class WebSocketEventPublisher implements EventReceiver, EventPublisher, WebSocket.OnTextMessage {

    private final WebSocketCloseEventListener closeEventListener;
    private EventPublisher eventPublisher;
    private Connection connection;
    private RequestParser parser;

    WebSocketEventPublisher(RunningComServer comServer, RequestParser.ServiceProvider serviceProvider, EventPublisher eventPublisher, WebSocketCloseEventListener closeEventListener) {
        super();
        this.closeEventListener = closeEventListener;
        this.eventPublisher = eventPublisher;
        this.parser = new RequestParser(comServer, serviceProvider);
    }

    @Override
    public void receive (ComServerEvent event) {
        this.publish(event);
    }

    public void answerPing(){
        this.sendMessage(RequestParser.PONG_MESSAGE);
    }

    @Override
    public void publish (ComServerEvent event) {
        this.sendEvent(event);
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

    private void sendEvent (ComServerEvent event) {
        try {
            if (this.isConnected()) {
                this.connection.sendMessage(event.toString());
            }
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
            this.sendMessage("Copy " + message);  //No sense to send the message back  - just to debug
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
        this.eventPublisher.unregisterAllInterests(this);
        this.closeEventListener.closedFrom(this);
    }

    @Override
    public void unregisterAllInterests (EventReceiver receiver) {
        // Unregistering only happens via onClose
    }

    @Override
    public void shutdown() {
        this.connection.close();
    }

    @Override
    public void registerInterest (EventReceiver receiver) {
        this.eventPublisher.registerInterest(this);
    }

    @Override
    public void narrowInterestToCategories (EventReceiver receiver, Set<Category> categories) {
        this.eventPublisher.narrowInterestToCategories(this, categories);
    }

    @Override
    public void narrowInterestToDevices (EventReceiver receiver, List<Device> devices) {
        this.eventPublisher.narrowInterestToDevices(this, devices);
    }

    @Override
    public void widenInterestToAllDevices (EventReceiver receiver) {
        this.eventPublisher.widenInterestToAllDevices(this);
    }

    @Override
    public void narrowInterestToConnectionTasks (EventReceiver receiver, List<ConnectionTask> connectionTasks) {
        this.eventPublisher.narrowInterestToConnectionTasks(this, connectionTasks);
    }

    @Override
    public void widenInterestToAllConnectionTasks (EventReceiver receiver) {
        this.eventPublisher.widenInterestToAllConnectionTasks(this);
    }

    @Override
    public void narrowInterestToComTaskExecutions (EventReceiver receiver, List<ComTaskExecution> comTaskExecutions) {
        this.eventPublisher.narrowInterestToComTaskExecutions(this, comTaskExecutions);
    }

    @Override
    public void widenInterestToAllComTaskExecutions (EventReceiver receiver) {
        this.eventPublisher.widenInterestToAllComTaskExecutions(this);
    }

    @Override
    public void narrowInterestToComPorts (EventReceiver receiver, List<ComPort> comPorts) {
        this.eventPublisher.narrowInterestToComPorts(this, comPorts);
    }

    @Override
    public void widenInterestToAllComPorts (EventReceiver receiver) {
        this.eventPublisher.widenInterestToAllComPorts(this);
    }

    @Override
    public void narrowInterestToComPortPools (EventReceiver receiver, List<ComPortPool> comPortPools) {
        this.eventPublisher.narrowInterestToComPortPools(this, comPortPools);
    }

    @Override
    public void widenInterestToAllComPortPools (EventReceiver receiver) {
        this.eventPublisher.widenInterestToAllComPortPools(this);
    }

    @Override
    public void narrowInterestToLogLevel (EventReceiver receiver, LogLevel logLevel) {
        this.eventPublisher.narrowInterestToLogLevel(this, logLevel);
    }

    @Override
    public void widenToAllLogLevels (EventReceiver receiver) {
        this.eventPublisher.widenToAllLogLevels(this);
    }

}