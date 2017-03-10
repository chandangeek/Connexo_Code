/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceSchedules', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceSchedule'
    ],
    model: 'Mdc.model.DeviceSchedule',
    storeId: 'DeviceSchedules',
    proxy: {
        type: 'rest',
        limitParam: false,
        pageParam: false,
        startParam: false,
        url: '/api/ddr/devices/{deviceName}/schedules',
        reader: {
            type: 'json',
            root: 'schedules'
        }
    }
});
