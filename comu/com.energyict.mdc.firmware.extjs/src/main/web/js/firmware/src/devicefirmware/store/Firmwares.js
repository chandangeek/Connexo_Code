/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.store.Firmwares', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.devicefirmware.model.Firmware'
    ],
    model: 'Fwc.devicefirmware.model.Firmware',
    storeId: 'DeviceFirmwares',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fwc/devices/{deviceId}/firmwares',
        reader: {
            type: 'json',
            root: 'firmwares',
            totalProperty: 'total'
        }
    }
});
