Ext.define('Mdc.store.LogbooksOfDevice', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.LogbookOfDevice',
    storeId: 'LogbooksOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/logbooks',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000
    }
});