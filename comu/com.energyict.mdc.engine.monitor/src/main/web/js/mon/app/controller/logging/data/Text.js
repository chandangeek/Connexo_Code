/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.logging.data.Text', {
    extend: 'CSMonitor.controller.logging.Text',

    requires: [
        'CSMonitor.controller.logging.Text'
    ],

    views: ['logging.data.Text'],

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'dataLoggingText'
        },
        {
            ref: 'mainContainer',
            selector: 'app-main'
        }
    ],

    init: function() {
        this.control({
            'dataLoggingText': {
                afterrender: this.onAfterRender
            },
            'dataLoggingText button#pauseLoggingBtn': {
                click: this.pauseLogging
            },
            'dataLoggingText button#saveLoggingBtn': {
                click: this.saveLogging
            },
            'dataLoggingText combobox#logLevelCombo': {
                select: this.onLogLevelChange
            }
        });
    },

    onAfterRender: function() {
        this.getViewPanel().setTitle('<h2>Data storage logging</h2>');
        this.getViewPanel().setUnselectable(); // Set everything unselectable except:
        this.getViewPanel().addLogPanelWithoutTitle();
     //   this.getViewPanel().setSaveLogBtnVisible(document.createElement('a').hasOwnProperty('download'));

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
        msg += ': COLLECTED_DATA_PROCESSING';
        this.getWebSocket().send(msg);
    },

    onTokens: function(tokens) {
        if (tokens[1] === 'data') {
            this.getMainContainer().addCenterPanel('dataLoggingText');
        }
    },

    saveLogging: function() {
        this.doSaveLogging("data_storage_logging.txt");
    }

});
