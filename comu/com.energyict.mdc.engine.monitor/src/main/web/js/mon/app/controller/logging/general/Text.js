/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.logging.general.Text', {
    extend: 'CSMonitor.controller.logging.Text',

    requires: [
        'CSMonitor.controller.logging.Text'
    ],

    views: ['logging.general.Text'],

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'generalLoggingText'
        },
        {
            ref: 'mainContainer',
            selector: 'app-main'
        }
    ],

    init: function() {
        this.control({
            'generalLoggingText': {
                afterrender: this.onAfterRender
            },
            'generalLoggingText button#pauseLoggingBtn': {
                click: this.pauseLogging
            },
            'generalLoggingText button#saveLoggingBtn': {
                click: this.saveLogging
            },
            'generalLoggingText combobox#logLevelCombo': {
                select: this.onLogLevelChange
            }
        });
    },

    onAfterRender: function() {
        this.getViewPanel().setTitle('<h2>General logging</h2>');
        this.getViewPanel().setUnselectable(); // Set everything unselectable except:
        this.getViewPanel().addLogPanelWithoutTitle();
  //      this.getViewPanel().setSaveLogBtnVisible(document.createElement('a').hasOwnProperty('download'));

        this.setPingTask(Ext.TaskManager.newTask({
            interval: this.getSecondsForNextPing() * 1000,
            scope: this,
            run: this.doPing
        }));

        this.subscribe();
    },

    registerForLogging: function() {
        var msg = 'register request for ';
        msg += this.getLevelDictionarySnd()[this.getViewPanel().getLogLevel()[0]];
        msg += ': LOGGING';
        this.getWebSocket().send(msg);
    },

    onTokens: function(tokens) {
        if (tokens[1] === 'general') {
            this.getMainContainer().addCenterPanel('generalLoggingText');
        }
    },

    saveLogging: function() {
        this.doSaveLogging("general_logging.txt");
    },

    fitsThisLogging: function(decorator) {
        // In General Logging we're interested in
        // CommunicationLoggingEvent & ComPortOperationsLoggingEvent
        // (rejecting: CommunicationLoggingEvent & ComCommandLoggingEvent)
        return "CommunicationLoggingEvent" === decorator.getEvent()['class']
            || "ComPortDiscoveryEvent" ===  decorator.getEvent()['class']
            || "ComPortOperationsLoggingEvent" === decorator.getEvent()['class'];
    }

});
