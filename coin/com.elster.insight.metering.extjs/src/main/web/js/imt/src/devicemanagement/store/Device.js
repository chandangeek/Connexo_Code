/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.devicemanagement.store.Device', {
    extend: 'Ext.data.Store',
    model: 'Imt.devicemanagement.model.Device',
    proxy: {
        type: 'rest',
        url: '/api/imt/devices/{deviceId}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterInfos'
        }
    }
});