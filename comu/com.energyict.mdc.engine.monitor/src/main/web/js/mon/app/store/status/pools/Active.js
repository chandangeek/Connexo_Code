Ext.define('CSMonitor.store.status.pools.Active', {
    extend: 'Ext.data.Store',
    model: 'CSMonitor.model.status.Pool',
    requires: ['Ext.data.proxy.Rest'],
    autoLoad: true,
    sortOnLoad: true,
    sorters: [{property: 'name'}],
    storeId: 'activePoolsStore',
    proxy: {
        type: 'rest',
        url: '/api/CSMonitor/monitoringResults/comPortPoolInfo/1',
        storeId: 'activePoolsStore',
        reader: {
            type: 'json',
            root: 'comPortPools',
            successProperty: 'success'
        }
    }
});