Ext.define('Mdc.store.Logbook', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.Logbook',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    pageSize: 25,
    autoLoad: false
});

