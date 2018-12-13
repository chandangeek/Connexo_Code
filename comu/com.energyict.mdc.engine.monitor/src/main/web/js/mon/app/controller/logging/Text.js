/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.logging.Text', {
    extend: 'Ext.app.Controller',
    requires: ['CSMonitor.decorators.logging.EventDecoratorFactory'],
    uses: ['CSMonitor.model.status.GeneralInformation'],
    config: {
        isConnected : false,
        isPaused : false,
        webSocket : null,
        subscribed : false,
        logLevels : ['Error', 'Warning', 'Info', 'Debug', 'Trace'],
        levelDictionarySnd : {'E' : 'errors', 'W' : 'warnings', 'I' : 'info', 'D' : 'debugging', 'T' : 'tracing'},
        levelDictionaryRcv : {'E' : 0, 'W' : 1, 'I' : 2, 'D' : 3, 'T' : 4},
        pingTask : null,
        secondsForNextPing : 3 * 60,  // 3 minutes
        messageClass : 'logmessage',
        errorMessageClass: 'logmessage-error'
    },

    views: ['logging.Text'],
    stores: ['status.GeneralInformation'],
    models: ['status.GeneralInformation'],

    // To be overridden in extending classes
    init: function() {
    },

    subscribe: function() {
        this.openWebSocket();
    },

    unsubscribe: function() {
        this.setSubscribed(false);
        this.getWebSocket().close();
    },

    onOpen: function(evt) {
        this.setIsConnected(true);
        this.registerForLogging();
        this.doLog('Connected and ' + this.getListeningForLevelsString(), this.getMessageClass());
    },

    // To be overridden in extending classes
    registerForLogging: function() {
    },

    // Can be overridden in extending classes
    registerForChangedLoggingLevel: function() {
        this.registerForLogging();
    },

    onLogLevelChange: function() {
        if (!this.getIsConnected()) {
            this.subscribe();
            return;
        }
        this.registerForChangedLoggingLevel();
        this.doLog('Now ' + this.getListeningForLevelsString(), this.getMessageClass());
    },

    onError: function(evt) {
        if (evt.data !== undefined) {
            this.doLog(evt.data, this.getErrorMessageClass());
        }
    },

    onClose: function(evt) {
        this.getPingTask().stop();
        this.setIsConnected(false);
        this.doLog('Disconnected.', this.getErrorMessageClass());
        this.doLogReadWriteMessage('Disconnected.', this.getErrorMessageClass());
        this.pauseLogging();
    },

    onMessage: function(evt) {
        // Something received from the server so reset the ping timer
        this.getPingTask().restart(this.getSecondsForNextPing() * 1000);

        if (this.getIsPaused()) {
            return; // Don't 'process' the received message
        }
        if (typeof evt.data === "string") {
            if ('pong' === evt.data) {
                console.log('Pong answer received from server');
                return;
            } else if (evt.data.length > 3 && 'Copy' === evt.data.substring(0, 4)) {
                console.log('String data received from server: ' + evt.data);
                return;
            }
            try {
                var decorator = EventDecoratorFactory.decorate(JSON.parse(evt.data));
                if (typeof decorator.getHex === 'function') {
                    this.doLogReadWriteEvent(decorator);
                } else {
                    if (this.fitsThisLogging(decorator)) {
                        this.doLog(decorator.asLogString());
                    }
                }
            } catch (e) {
                console.log(e);
                this.doLog("Communication server says: " + evt.data,
                    (evt.data.indexOf("not understood") !== -1) ? this.getErrorMessageClass() : this.getMessageClass);
            }
        } else if (evt.data instanceof Blob) {
            console.log("Blob data received");
        }
    },

    openWebSocket: function(){
        var socketProtocol = 'ws:' ;
        if (window.location.protocol === 'https'){
            socketProtocol = 'wss:';
        }

        var me = this;
        this.getStatusGeneralInformationStore().load({
            callback: function(records, operation, success) {
                if (success) {
                    var url =  me.getStatusGeneralInformationStore().first().get('eventRegistrationUri');
                    Ext.create('CSMonitor.util.UriParser').parse(url).withProtocol(socketProtocol).buildUrl();

                    me.setWebSocket(new WebSocket(url));
                    me.getWebSocket().onopen = function(evt) { me.onOpen(evt); };
                    me.getWebSocket().onclose = function(evt) { me.onClose(evt); };
                    me.getWebSocket().onmessage = function(evt) { me.onMessage(evt); };
                    me.getWebSocket().onerror = function(evt) { me.onError(evt); };
                    me.setSubscribed(true);
                    me.getPingTask().start();
                } else {
                    console.log("statusGeneralInformationStore.load() was UNsuccessful. WebSocket not opened");
                }
            }
        });
    },

    fitsThisLogging: function(decorator) {
        return true;
    },

    doLog: function(message, cssClass) {
        if (cssClass) {
            this.getViewPanel().logMessage('<span class="' + cssClass + '">' + message + '</span>');
        } else {
            this.getViewPanel().logMessage(message);
        }
    },

    doLogReadWriteEvent: function(decorator) {
        if (typeof this.getBytesViewPanel === 'function') {
            if (this.getBytesViewPanel()) {
                this.getBytesViewPanel().logReadWriteEvent(decorator);
            }
        }
    },

    doLogReadWriteMessage: function(message, cssClass) {
        if (typeof this.getBytesViewPanel === 'function') {
            if (this.getBytesViewPanel()) {
                if (cssClass) {
                    this.getBytesViewPanel().logMessage('<span class="' + cssClass + '">' + message + '</span>');
                } else {
                    this.getBytesViewPanel().logMessage(message);
                }
            }
        }
    },

    doPing: function() {
        this.getWebSocket().send('ping');
    },

    // ===============
    // Helper methods
    // ===============

    // Returns a number from 0 to 4 indicating the logging level we're interested in
    getCurrentLogLevel: function() {
        return this.getLevelDictionaryRcv()[this.getViewPanel().getLogLevel()[0]];
    },

    // Returns a number from 0 to 4 corresponding with the passed in logging level as string
    getLogLevel: function(levelString) {
        return this.getLevelDictionaryRcv()[levelString[0]];
    },

    getListeningForLevelsString: function() {
        var msg = 'listening for: ',
            currentLevel = this.getCurrentLogLevel();
        while (currentLevel >= 0) {
            msg += this.getLogLevels()[currentLevel];
            currentLevel -= 1;
            if (currentLevel === 0) {
                msg += ' & ';
            } else if (currentLevel > 0) {
                msg += ', ';
            }
        }
        msg += '...';
        return msg;
    },

    pauseLogging: function() {
        this.setIsPaused(!this.getIsPaused());
        if (this.getIsPaused()) {
            this.doLog('Listening stopped', this.getMessageClass());
            this.doLogReadWriteMessage('Listening stopped', this.getMessageClass());
        } else {
            if (!this.getIsConnected()) {
                this.subscribe();
                return;
            }
            this.doLog('Listening started', this.getMessageClass());
            this.doLogReadWriteMessage('Listening started', this.getMessageClass());
        }
        this.getViewPanel().setLoggingIsPaused(this.getIsPaused());
    },

    doSaveLogging: function(fileName) {
        var mime_type = 'text/plain;charset=utf-8',
            loggingData = new Blob([this.getViewPanel().getLogging()], {type: mime_type}),
            anchor = document.createElement('a');

        anchor.style.display = "none";
        anchor.href = window.URL.createObjectURL(loggingData);
        anchor.download =  fileName;
        anchor.dataset.downloadurl = [mime_type, anchor.download, anchor.href].join(':');
        // Firefox requires the link to be added to the DOM
        // before it can be clicked.
        document.body.appendChild(anchor);
        anchor.click();
        document.body.removeChild(anchor);
    }
});
