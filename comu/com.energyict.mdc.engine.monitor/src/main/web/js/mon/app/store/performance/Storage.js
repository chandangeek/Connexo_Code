Ext.define('CSMonitor.store.performance.Storage', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.performance.Storage',
    requires: ['Ext.data.proxy.Rest'],
    storeId: 'storageStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/collectedDataStorageStatistics',
        storeId: 'storageStore',
        reader: {
            type: 'json',
            root: 'data',
            successProperty: 'success'
        }
    }

});