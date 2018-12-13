/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.status.pools.Active', {
    extend: 'Ext.app.Controller',

    stores: ['status.pools.Active'],
    models: ['status.Pool'],
    views: ['status.pools.Active'],

    init: function() {
        this.control({
            'activePools': {
                afterrender: this.refreshData
            },
            'generalInformation button#refreshBtn': {
                click: this.refreshData
            }
        });
    },

    refreshData: function() {
        this.getStatusPoolsActiveStore().load();
    }
});