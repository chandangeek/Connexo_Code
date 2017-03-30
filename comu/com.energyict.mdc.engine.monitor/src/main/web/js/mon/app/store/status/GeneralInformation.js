/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.store.status.GeneralInformation', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.status.GeneralInformation',
    requires: ['Ext.data.proxy.Rest'],
    autoLoad: true,
    storeId: 'generalInfoStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/generalInfo',
        storeId: 'generalInfoStore',
        reader: {
            type: 'json',
            root: 'data',
            successProperty: 'success'
        }
    }
});