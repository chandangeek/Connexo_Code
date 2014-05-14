Ext.define('Mdc.store.LogbookTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Mdc.model.LogbookTypes'
    ],
    model: 'Mdc.model.LogbookTypes',
    storeId: 'LogbookTypes',
    autoLoad: false
});
