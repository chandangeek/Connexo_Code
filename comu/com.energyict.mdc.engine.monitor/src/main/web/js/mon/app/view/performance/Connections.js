/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.performance.Connections', {
    extend: 'Ext.panel.Panel',
    xtype: 'connections',
    border: false,
    layout: 'vbox',

    items: [
        {
            xtype: 'component',
            margins: '0 0 0 10',
            itemId: 'scheduling',
            html: '<h2>Scheduling [outbound]</h2>'
        },
        {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    xtype: 'container',
                    layout: 'vbox',
                    defaults : { margins: '2 0 2 20'},
                    items: [
                        {
                            xtype: 'component',
                            itemId: 'threads',
                            html: 'threads:'
                        }
                    ]
                },
                {
                    itemId: 'threadsChart',
                    xtype: 'threadsChart'
                }
            ]
        }
    ],

    setNumberOfThreads: function(numberOfThreads) {
        var threadsText = "Total number of threads";
        this.down('#threads').update(threadsText + ': <b>' + numberOfThreads + '</b>');
    }
});