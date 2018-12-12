/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.store.status.RunningInformation', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.status.RunningInformation',
    requires: ['Ext.data.proxy.Rest'],
    autoLoad: true,
    storeId: 'runningInfoStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/runningInfo',
        storeId: 'runningInfoStore',
        reader: {
            type: 'json',
            root: 'data',
            successProperty: 'success'
        }
    }
});