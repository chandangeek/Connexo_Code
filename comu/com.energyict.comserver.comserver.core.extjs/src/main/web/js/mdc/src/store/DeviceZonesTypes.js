/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceZonesTypes', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/zones/remainingZoneTypes',
        timeout: 250000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'remainingZoneTypes'
        },
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }
});

