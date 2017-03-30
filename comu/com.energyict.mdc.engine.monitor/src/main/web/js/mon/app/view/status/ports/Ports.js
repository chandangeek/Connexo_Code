/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.ports.Ports', {
    extend: 'Ext.panel.Panel',
    xtype: 'ports',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: false,

    items: [
        {
            xtype: 'component',
            margins: '0 0 0 10',
            itemId: 'statusComPorts',
            html: '<h2>Communication ports</h2>'
        },
        {
            xtype: 'container',
            layout: {
                type : 'hbox',
                align: 'stretch',
                autoSize: true
            },

            margins: '0 10 0 10',
            defaults : { margins: '0 10 0 10' },
            items: [
                {
                    xtype: 'activePorts',
                    flex: 2
                },
                {
                    xtype: 'inactivePorts',
                    flex: 1
                }
            ]
        }
    ]
});
