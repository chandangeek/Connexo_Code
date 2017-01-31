/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycles.store.DeviceLifeCycles', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecycles.model.DeviceLifeCycle',
    autoLoad: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles',
        timeout: 300000,
        reader: {
            type: 'json',
            root: 'deviceLifeCycles'
        }
    }
});