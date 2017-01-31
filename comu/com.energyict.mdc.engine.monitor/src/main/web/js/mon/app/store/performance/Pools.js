/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.store.performance.Pools', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.performance.PoolsPerformance',
    requires: ['Ext.data.proxy.Rest'],
    autoLoad: true,
    storeId: 'poolsStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/comPortPoolGraphInfo',
        storeId: 'poolsStore',
        reader: {
            type: 'json',
            successProperty: 'success'
        }
    }
});