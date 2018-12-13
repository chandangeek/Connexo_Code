/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.ServerDetails', {
    extend: 'Ext.panel.Panel',
    requires: ['Ext.layout.container.Column'],
    xtype: 'serverDetails',
    layout: {
        type: 'vbox',       // Arrange child items vertically
        align: 'stretch'    // Each takes up full width
    },
    border: false,
    defaults: {
        cls: 'server-details',
    },
    items: [
        {
            xtype: 'component',
            itemId: 'serverDetailsTitle',
            html: '<h1>Communication server</h1>'
        },
        {
            xtype: 'form',
            defaults:{
                xtype: 'displayfield',
                labelCls: 'server-details',
                fieldCls: 'server-details',
                labelWidth: 180,
                width: 500
            },
            baseCls: 'server-details',
            items: [{
                fieldLabel: 'Started',
                itemId: 'serverStarted',
                name: 'started'
            },
            {
                fieldLabel: 'Date of this information',
                itemId: 'serverDateInfo',
                name: 'currentDate'
            }]
        }
    ],

    setServerDetails: function(serverDetails) {
        var comServerText = "Communication server",
            serverName = serverDetails.get('serverName');

        this.down('#serverDetailsTitle').update('<h1>' + comServerText + ' ' + serverName + '</h1>');
        this.down('form').loadRecord(serverDetails);
    },

    setUnselectable: function() {
        this.getEl().unselectable();
    }
});
