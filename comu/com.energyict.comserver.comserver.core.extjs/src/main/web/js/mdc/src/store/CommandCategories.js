Ext.define('Mdc.store.CommandCategories',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommandCategory'
    ],
    model: 'Mdc.model.CommandCategory',
    storeId: 'CommandCategories',
    autoLoad: false,
    sorters: {
        property: 'name',
        direction: 'ASC'
    },
    proxy: {
        type: 'rest',
        url: '/api/crr/commandrules/categories',
        reader: {
            type: 'json'
        }
    }
});