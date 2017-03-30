/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.store.status.ports.Active', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.status.Port',
    requires: ['Ext.data.proxy.Rest'],
    autoLoad: true,
    sortOnLoad: true,
    sorters: [{property: 'name'}],
    storeId: 'activePortsStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/comPortInfo/1',
        storeId: 'activePortsStore',
        reader: {
            type: 'json',
            root: 'comPorts',
            successProperty: 'success'
        }
    }
});