/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

Ext.define('Fwc.devicefirmware.store.DeviceFirmwareHistoryStore', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.devicefirmware.model.DeviceFirmwareHistoryModel'
    ],
    model: 'Fwc.devicefirmware.model.DeviceFirmwareHistoryModel',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devices/{deviceId}/firmwares/firmwarehistory',
        reader: {
            type: 'json',
            root: 'firmwareHistory'
            // totalProperty: 'total'
        },
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }

});