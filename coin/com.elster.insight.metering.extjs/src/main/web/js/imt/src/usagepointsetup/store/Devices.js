/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointsetup.store.Devices', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointsetup.model.Device',
    pageSize: 50,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/udr/devices/available',
        start: 0,
        limit: 50,
        reader: {
            type: 'json',
            root: 'meterInfos'
        }
    }
});
