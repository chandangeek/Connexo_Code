/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.store.status.servers.Active', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.status.Server',
    requires: ['Ext.data.proxy.Rest'],
    autoLoad: true,
    sortOnLoad: true,
    sorters: [{property: 'name'}],
    storeId: 'activeServersStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/remoteServers/1',
        storeId: 'activeServersStore',
        reader: {
            type: 'json',
            root: 'servers',
            successProperty: 'success'
        }
    }
});