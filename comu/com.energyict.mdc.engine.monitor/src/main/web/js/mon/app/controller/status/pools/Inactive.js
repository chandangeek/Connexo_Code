/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.status.pools.Inactive', {
    extend: 'Ext.app.Controller',

    stores: ['status.pools.Inactive'],
    models: ['status.Pool'],
    views: ['status.pools.Inactive'],

    init: function() {
        this.control({
            'inactivePools': {
                afterrender: this.refreshData
            },
            'generalInformation button#refreshBtn': {
                click: this.refreshData
            }
        });
    },

    refreshData: function() {
        this.getStatusPoolsInactiveStore().load();
    }

});