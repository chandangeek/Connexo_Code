/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.servers.Connected', {
    extend: 'Ext.panel.Panel',
    xtype: 'connectedServers',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: false,

    items: [
        {
            xtype: 'component',
            margins: '0 0 0 10',
            html: '<h2>Connected remote communication servers</h2>'
        },
        {
            xtype: 'container',
            layout: {
                type : 'hbox',
                autoSize: true
            },

            margins: '0 10 0 10',
            defaults : { margins: '0 10 0 10' },
            items: [
                {
                    xtype: 'activeServers',
                    flex: 2
                },
                {
                    xtype: 'inactiveServers',
                    flex: 1
                }
            ]
        }
    ]
});
