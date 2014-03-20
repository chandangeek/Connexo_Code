Ext.define('Isu.store.AutoCreationRules', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.AutoCreationRules',
    pageSize: 100,
    autoLoad: false
});
