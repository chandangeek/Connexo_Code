/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.RunningInformation', {
    extend: 'Ext.panel.Panel',
    requires: ['Ext.ProgressBar'],
    xtype: 'runningInformation',
    border: false,
    layout: 'vbox',

    items: [
        {
            xtype: 'component',
            itemId: 'statusRunningInfo',
            margins: '0 0 0 10',
            html: '<h2>Running information</h2>'
        },
        {
            xtype: 'container',
            layout: 'vbox',
            defaults : { margins: '2 0 2 20'},
            items: [
                {
                    xtype: 'component',
                    itemId: 'events',
                    html: 'Total amount of events:'
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    items: [
                        {
                            xtype: 'component',
                            html: 'Memory usage:'
                        },
                        {
                            xtype: 'progressbar',
                            width: 300,
                            margins: '0 0 0 10',
                            itemId: 'memoryUsageBar',
                            animate: true
                        },
                        {
                            xtype: 'component',
                            itemId: 'memoryUsageText',
                            margins: '0 0 0 10',
                            html: 'x of y GB'
                        }
                    ]
                }
            ]
        }
    ],

    setRunningInformation: function(runningInfo) {
        var eventsText = "Total amount of events",
            usedMemory = parseFloat(runningInfo.get('usedMemory')),
            maxMemory = parseFloat(runningInfo.get('maxMemory'));

        this.down('#events').update(
            eventsText + ': <b>' + runningInfo.get('numberOfEvents') + '</b>'
        );
        this.down('#memoryUsageBar').updateProgress(usedMemory / maxMemory);
        this.down('#memoryUsageText').update('<b>' + runningInfo.get('usedMemory') + ' of ' + runningInfo.get('maxMemory') + runningInfo.get('unit') +
                ' (' + runningInfo.get('usedMemory') + ' = current memory allocated ' + runningInfo.get('maxMemory') + ' = max heap space)</b>');
    }
});