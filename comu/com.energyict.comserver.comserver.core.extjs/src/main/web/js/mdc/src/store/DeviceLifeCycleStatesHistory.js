/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceLifeCycleStatesHistory', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.DeviceLifeCycleStatesHistory',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/history/devicelifecyclechanges',
        reader: {
            type: 'json',
            root: 'deviceLifeCycleChanges'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});
