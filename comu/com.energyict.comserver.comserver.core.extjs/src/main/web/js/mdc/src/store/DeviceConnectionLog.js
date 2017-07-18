/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceConnectionLog', {
    extend: 'Uni.data.store.Filterable',
    storeId: 'deviceConnectionLog',
    requires: ['Mdc.model.DeviceConnectionLog'],
    model: 'Mdc.model.DeviceConnectionLog',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connectionmethods/{connectionId}/comsessions/{sessionId}/journals',
        timeout: 250000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'journals'
        }
    }
});