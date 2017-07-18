/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceCommands', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceCommand'
    ],
    model: 'Mdc.model.DeviceCommand',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/devicemessages',
        reader: {
            type: 'json',
            root: 'deviceMessages'
        }
    }
});