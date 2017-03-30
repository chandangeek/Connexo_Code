/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.Status', {
    extend: 'Ext.panel.Panel',
    xtype: 'status',
    layout: {
        type : 'vbox',
        align : 'stretch'
    },
    autoScroll: true, // show scroll bars whenever needed
    border: false,
    items: [
        {
            xtype: 'generalInformation'
        },
        {
            xtype: 'runningInformation'
        },
       // TODO : Remote Communication Servers not yet implemented in Connexo Multisense
       /* {
            itemId: 'remoteServersPnl',
            xtype: 'connectedServers'
        },  */
        {
            xtype: 'ports'
        },
        {
            xtype: 'pools'
        }
    ],

    setVisibilityOfRemoteServers: function(visible) {
        this.down('#remoteServersPnl').setVisible(visible);
    }
});
