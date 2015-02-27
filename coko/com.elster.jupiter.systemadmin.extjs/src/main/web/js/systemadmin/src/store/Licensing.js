Ext.define('Sam.store.Licensing', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Sam.model.Licensing',
    pageSize: 10,
    autoLoad: false
});

