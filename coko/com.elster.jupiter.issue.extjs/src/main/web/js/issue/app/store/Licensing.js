Ext.define('Isu.store.Licensing', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Licensing',
    pageSize: 10,
    autoLoad: false,
    sorters: { property: 'expires', direction : 'ASC' }
});

