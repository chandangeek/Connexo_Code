/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceZones', {
    extend: 'Ext.data.Store',
    requires: ['Mdc.model.DeviceZones'],
    model: 'Mdc.model.DeviceZones',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/zones',
        /*timeout: 250000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,*/
        reader: {
            type: 'json',
            root: 'zones'
        },
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }
});

