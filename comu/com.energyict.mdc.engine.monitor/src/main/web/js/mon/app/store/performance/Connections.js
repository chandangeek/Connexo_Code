/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.store.performance.Connections', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.performance.Connections',
    requires: ['Ext.data.proxy.Rest'],
    autoLoad: true,
    storeId: 'connectionsStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/threadsInUse',
        storeId: 'connectionsStore',
        reader: {
            type: 'json',
            root: 'data',
            successProperty: 'success'
        }
    }
});