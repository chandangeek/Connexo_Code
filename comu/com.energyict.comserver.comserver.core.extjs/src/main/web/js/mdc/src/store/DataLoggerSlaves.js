/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DataLoggerSlaves', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'DataLoggerSlaves',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'dataLoggerSlaveDevices'
        },
        url: '/api/ddr/devices/{deviceId}/dataloggerslaves'
    }
});
