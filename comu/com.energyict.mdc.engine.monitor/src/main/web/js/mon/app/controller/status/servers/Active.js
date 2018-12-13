/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.status.servers.Active', {
    extend: 'Ext.app.Controller',

    stores: ['status.servers.Active'],
    models: ['status.Server'],
    views: ['status.servers.Active'],

    init: function() {
        this.control({
            'activeServers': {
                afterrender: this.refreshData
            },
            'generalInformation button#refreshBtn': {
                click: this.refreshData
            }
        });
    },

    refreshData: function() {
        this.getStatusServersActiveStore().load();
    }
});