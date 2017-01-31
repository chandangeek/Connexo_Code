/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.store.status.ports.Inactive', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.status.Port',
    requires: ['Ext.data.proxy.Rest'],
    autoLoad: true,
    sortOnLoad: true,
    sorters: [{property: 'name'}],
    storeId: 'inactivePortsStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/comPortInfo/0',
        storeId: 'inactivePortsStore',
        reader: {
            type: 'json',
            root: 'comPorts',
            successProperty: 'success'
        }
    }
});