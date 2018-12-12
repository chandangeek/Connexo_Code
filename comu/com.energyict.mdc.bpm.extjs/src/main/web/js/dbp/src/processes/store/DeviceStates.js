/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.store.DeviceStates', {
    extend: 'Ext.data.Store',
    model: 'Dbp.processes.model.DeviceState',
    autoLoad: false,
    proxy: {
        type: 'rest',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        url: '/api/dld/devicelifecycles/states',
        reader: {
            type: 'json',
            root: 'deviceStates'
        }
    }
});
