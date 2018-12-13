/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.performance.Storage', {
    extend: 'Ext.panel.Panel',
    requires: ['Ext.ProgressBar'],
    xtype: 'storage',
    border: false,
    layout: {
        type : 'vbox',
        align : 'stretch'
    },

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
                    itemId: 'dataStorage',
                    html: '<h2>Data storage</h2>'
                },
                {
                    xtype: 'component',
                    flex: 1
                },
                {
                    xtype: 'progressbar',
                    width: 150,
                    margins: '0 10 0 0',
                    animate: true,
                    itemId: 'performanceProgressBar'
                },
                {
                    xtype: 'button',
                    margins: '0 10 0 0',
                    text: 'Refresh',
                    itemId: 'performanceRefreshBtn'
                }
            ]
        },
        {
            xtype: 'container',
            layout: {
                type : 'vbox',
                align : 'stretch'
            },
            defaults : { margins: '2 0 2 20'},
            items: [
                {
                    xtype: 'container',
                    itemId: 'priorityContainer',
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    items: [
                        {
                            xtype: 'component',
                            itemId: 'priorityLabel',
                            html: 'Priority:'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '1',
                            margins: '0 0 0 10',
                            itemId: 'progressBar1'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '2',
                            itemId: 'progressBar2'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '3',
                            itemId: 'progressBar3'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '4',
                            itemId: 'progressBar4'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '5',
                            itemId: 'progressBar5'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '6',
                            itemId: 'progressBar6'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '7',
                            itemId: 'progressBar7'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '8',
                            itemId: 'progressBar8'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '9',
                            itemId: 'progressBar9'
                        },
                        {
                            xtype: 'progressbar',
                            width: 30,
                            value: 0,
                            text: '10',
                            itemId: 'progressBar10'
                        },
                        {
                            xtype: 'component',
                            margins: '2 0 2 20',
                            itemId: 'priorityLowHigh',
                            html: '(1 = Low / 10 = High)'
                        }
                    ]
                },
                {
                    xtype: 'storageChart',
                    itemId: 'storageChart'
                }
            ]
        }
    ],

    setPriority: function(priority) {
        var label, i;
        for (i = 1; i <= 10; i += 1) {
            label = '#progressBar' + i;
            this.down(label).updateProgress(0);
        }
        label = '#progressBar' + priority;
        if (this.down(label)) {
            this.down(label).updateProgress(1);
        }
    },

    setWaitInfo: function(secondsToWait, refreshRateInSeconds, text) {
        if (secondsToWait === 0 && refreshRateInSeconds === 0) {
            this.down('#performanceProgressBar').setVisible(false);
        } else {
            this.down('#performanceProgressBar').setVisible(true);
            this.down('#performanceProgressBar').updateProgress(secondsToWait / refreshRateInSeconds, text);
        }
    }

});