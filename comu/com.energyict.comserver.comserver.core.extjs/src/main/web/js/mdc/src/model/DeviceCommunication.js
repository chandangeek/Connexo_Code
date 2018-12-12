/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceCommunication', {
    extend: 'Mdc.model.DeviceCommunicationTask',

    fields: [
        'latestResult',
        'lastCommunicationStart',
        'isOnHold'
    ],

    run: function(callback, body) {
        Ext.Ajax.request({
            method: 'PUT',
            isNotEdit: true,
            jsonData: body,
            url: this.buildUrl(body.device.name) + '/run',
            callback: callback
        });
    },

    runNow: function(callback, body) {
        Ext.Ajax.request({
            method: 'PUT',
            isNotEdit: true,
            jsonData: body,
            url: this.buildUrl(body.device.name) + '/runnow',
            callback: callback
        });
    },

    activate: function(callback, body) {
        Ext.Ajax.request({
            method: 'PUT',
            isNotEdit: true,
            jsonData: body,
            url: this.buildUrl(body.device.name) + '/activate',
            callback: callback
        });
    },

    deactivate: function(callback, body) {
        Ext.Ajax.request({
            method: 'PUT',
            isNotEdit: true,
            jsonData: body,
            url: this.buildUrl(body.device.name) + '/deactivate',
            callback: callback
        });
    },

    buildUrl: function (deviceId) {
        return this.proxy.url.replace('{deviceId}', deviceId) + '/' + this.get('comTask').id;
    },

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/comtasks',
        reader: {
            type: 'json',
            root: 'comTasks'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});