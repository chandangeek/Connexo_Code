Ext.define('Sam.store.DataPurgeSettings', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Sam.model.DataPurgeSetting',
    pageSize: 10,
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/lic/data/lifecycle/categories',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});