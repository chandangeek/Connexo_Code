/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.store.ServerDetails', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.ServerDetails',
    autoLoad: true,
    requires: ['Ext.data.proxy.Rest'],
    storeId: 'serverDetailsStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/serverDetails',
        storeId: 'serverDetailsStore',
        reader: {
            type: 'json',
            root: 'data',
            successProperty: 'success'
        },
        listeners: {
            exception: function(proxy, response, operation, eOpts) {
                if (response){
                    var responseObject = Ext.decode(response.responseText);
                    console.log(responseObject);
                }
            }
        }
    }
});