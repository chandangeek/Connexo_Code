/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.store.FirmwareActions', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.devicefirmware.model.FirmwareAction'
    ],
    model: 'Fwc.devicefirmware.model.FirmwareAction',
    storeId: 'DeviceFirmwareActions',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fwc/devices/{deviceId}/firmwaresactions',
        reader: {
            type: 'json',
            root: 'firmwareactions',
            totalProperty: 'total'
        }
    }
});
