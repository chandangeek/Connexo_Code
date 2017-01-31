/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.logging.Text', {
    extend: 'Ext.panel.Panel',
    border: true,

    requires: [
        'Ext.form.field.ComboBox',
        'CSMonitor.view.logging.Viewer',
        'Ext.layout.container.Border'
    ],

    config: {
        linesLogged: 0,
        maxLines: 10000,
        breakCode: '<br>',
        logWindowBody: null,
        logWindowChild: null,
        scrollExecRate: 500, // limit to one call every "n" msec
        scrollLastExec: null,
        scrollTask: null
    },

    views: [
        'logging.Viewer'
    ],

    layout: {
        type: 'border'
    },

    items: [
        {
            xtype: 'panel',
            region: 'north',
            itemId: 'northPnl',
            bodyPadding: '0, 5, 0, 5',
            items: [
                {
                    xtype: 'container',
                    margins: '0 0 0 10',
                    height: 52,
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    items: [
                        {
                            xtype: 'component',
                            itemId: 'loggingTitle',
                            margins: '0 20 0 0',
                            html: ''
                        },
                        {
                            xtype: 'component',
                            itemId: 'selectionCriteriaInfo',
                            html: ''
                        },
                        {
                            xtype: 'component',
                            flex: 1
                        },
                        {
                            xtype: 'component',
                            margins: '0 5 0 0',
                            html: 'Log level:'
                        },
                        {
                            xtype: 'combobox',
                            itemId: 'logLevelCombo',
                            margins: '0 10 0 0',
                            store : {
                                fields: ['logLevel'],
                                data : [
                                    {"logLevel": "Error"},
                                    {"logLevel": "Warning"},
                                    {"logLevel": "Info"},
                                    {"logLevel": "Debug"},
                                    {"logLevel": "Trace"}
                                ]
                            },
                            value: 'Trace',
                            queryMode: 'local',
                            displayField: 'logLevel',
                            editable: false
                        },
                        {
                            xtype: 'button',
                            text: 'Stop logging',
                            itemId: 'pauseLoggingBtn',
                            margins: '0 10 0 0'
                        },
                        {
                            xtype: 'button',
                            text: 'Save logging',
                            itemId: 'saveLoggingBtn'
                        }
                    ]
                }
            ]
        }
    ],

    addLogPanel: function(title) {
        this.add(
            {
                region: 'center',
                xtype: 'logViewer',
                floatable: false,
                bodyPadding: '0, 3, 0, 3',
                itemId: 'logWindow',
                title: title,
                flex: 1
            }
        );
    },

    addLogPanelWithoutTitle: function() {
        this.addLogPanel(undefined);
    },

    logMessage: function(content) {
        if (this.getLinesLogged() >= this.getMaxLines()) {
            this.getLogWindowChild().removeChild(this.getLogWindowChild().childNodes[0]);
        } else {
            this.setLinesLogged(this.getLinesLogged() + 1);
        }

        var myDivHex = document.createElement('div');
        myDivHex.innerHTML = content;
        myDivHex.innerHTML += this.getBreakCode();
        this.getLogWindowChild().appendChild(myDivHex);

        this.scrollToBottom();
    },

    getLogLevel: function() {
        return this.down('#logLevelCombo').getValue();
    },

    setLoggingIsPaused: function(isPaused) {
        if (isPaused) {
            this.down('#pauseLoggingBtn').setText('Start logging');
        } else {
            this.down('#pauseLoggingBtn').setText('Stop logging');
        }
    },

    setSelectionCriteria: function(selectionCriteria) {
        this.down('#selectionCriteriaInfo').update('Filter criteria: ' + selectionCriteria);
    },

    setTitle: function(title) {
        this.down('#loggingTitle').update(title);
    },

    setUnselectable: function() {
        this.doSetUnselectable('#northPnl');
        this.doSetUnselectable('#loggingTitle');
        this.doSetUnselectable('#selectionCriteriaInfo');
        this.doSetUnselectable('#logLevelCombo');
        this.doSetUnselectable('#pauseLoggingBtn');
    },

    doSetUnselectable: function(itemId) {
        if (this.down(itemId)) {
            if (this.down(itemId).getEl() !== undefined) {
                this.down(itemId).getEl().unselectable();
            }
        }
    },

    setSaveLogBtnVisible: function(visible) {
        if (this.down('#saveLoggingBtn')) {
            this.down('#saveLoggingBtn').setVisible(visible);
        }
    },

    getLogging: function() {
        return this.getLogWindowBody().dom.firstChild.firstChild.textContent;
    },

    getLogWindowBody: function() {
        if (!this.logWindowBody) {
            this.logWindowBody = this.down('#logWindow').body;
        }
        return this.logWindowBody;
    },

    getLogWindowChild: function() {
        if (!this.logWindowChild) {
            this.logWindowChild = this.getLogWindowBody().dom.firstChild.firstChild;
        }
        return this.logWindowChild;
    },

    scrollToBottom: function() {
        if (!this.getScrollLastExec()) { // first time
            this.setScrollTask(Ext.TaskManager.newTask({
                interval: this.getScrollExecRate(),
                scope: this,
                run: this.scrollToBottom
            }));
            this.setScrollLastExec(new Date());
            this.getLogWindowBody().dom.scrollTop = 999999; // Scroll to the bottom
            return;
        }

        var d = new Date();
        if (d - this.getScrollLastExec() < this.getScrollExecRate()) {
            // This function has been called "too soon," (ie. before the allowed "rate")
            this.getScrollTask().stop();
            this.getScrollTask().start();
            return;
        }
        this.getScrollTask().stop();
        this.setScrollLastExec(new Date());
        // Only one scroll needed since both are synced:
        this.getLogWindowBody().dom.scrollTop = 999999; // Scroll to the bottom
    }
});