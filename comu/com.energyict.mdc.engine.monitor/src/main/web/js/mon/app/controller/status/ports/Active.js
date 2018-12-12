/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.status.ports.Active', {
    extend: 'Ext.app.Controller',

    stores: ['status.ports.Active'],
    models: ['status.Port'],
    views: ['status.ports.Active'],

    refs: [
        {
            ref: 'comViewPanel',
            selector: 'communication'
        }
    ],

    init: function() {
        this.control({
            'activePorts': {
                afterrender: this.refreshData
            },
            'generalInformation button#refreshBtn': {
                click: this.refreshData
            }
        });
    },

    refreshData: function() {
        this.getStatusPortsActiveStore().load();
    }
});