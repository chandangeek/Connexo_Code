/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.store.status.pools.Inactive', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.status.Pool',
    requires: ['Ext.data.proxy.Rest'],
    autoLoad: true,
    sortOnLoad: true,
    sorters: [{property: 'name'}],
    storeId: 'inactivePoolsStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/comPortPoolInfo/0',
        storeId: 'inactivePoolsStore',
        reader: {
            type: 'json',
            root: 'comPortPools',
            successProperty: 'success'
        }
    }
});