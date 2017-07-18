/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceFirmwareHistory', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.DeviceFirmwareHistory',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/history/firmwarechanges',
        reader: {
            type: 'json',
            root: 'deviceFirmwareHistoryInfos'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});
