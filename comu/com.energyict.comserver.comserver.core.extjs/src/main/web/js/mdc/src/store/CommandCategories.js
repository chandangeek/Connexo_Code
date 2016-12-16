Ext.define('Mdc.store.CommandCategories',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommandCategory'
    ],
    model: 'Mdc.model.CommandCategory',
    storeId: 'CommandCategories',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/crr/commandrules/categories',
        reader: {
            type: 'json'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});