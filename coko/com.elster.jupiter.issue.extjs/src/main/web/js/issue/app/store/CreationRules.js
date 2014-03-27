Ext.define('Isu.store.CreationRules', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.CreationRules',
    pageSize: 10,
    autoLoad: false
});
