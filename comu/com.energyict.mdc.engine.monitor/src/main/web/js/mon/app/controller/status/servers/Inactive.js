/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.status.servers.Inactive', {
    extend: 'Ext.app.Controller',

    stores: ['status.servers.Inactive'],
    models: ['status.Server'],
    views: ['status.servers.Inactive'],

    init: function() {
        this.control({
            'inactiveServers': {
                afterrender: this.refreshData
            },
            'generalInformation button#refreshBtn': {
                click: this.refreshData
            }
        });
    },

    refreshData: function() {
        this.getStatusServersInactiveStore().load();
    }

});