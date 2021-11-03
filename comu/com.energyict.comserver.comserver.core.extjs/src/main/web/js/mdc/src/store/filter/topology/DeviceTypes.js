/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.filter.topology.DeviceTypes', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/topology/communication/devicetypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});