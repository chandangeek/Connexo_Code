/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceStatePrivileges', {
    extend: 'Ext.data.Store',

    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/privileges',

        reader: {
            type: 'json',
            root: 'privileges'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});