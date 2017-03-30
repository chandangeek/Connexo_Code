/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.pools.Pools', {
    extend: 'Ext.panel.Panel',
    xtype: 'pools',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: false,

    items: [
        {
            xtype: 'component',
            margins: '0 0 0 10',
            itemId: 'statusComPortPools',
            html: '<h2>Communication port pools</h2>'
        },
        {
            xtype: 'container',
            layout: {
                type : 'hbox',
                align: 'stretch',
                autoSize: true
            },

            margins: '0 10 0 10',
            defaults : { margins: '0 10 0 10', flex: 1 },
            items: [
                {
                    xtype: 'activePools'
                },
                {
                    xtype: 'inactivePools'
                }
            ]
        }
    ]
});
