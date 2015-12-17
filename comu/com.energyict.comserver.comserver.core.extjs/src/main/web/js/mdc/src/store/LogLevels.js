Ext.define('Mdc.store.LogLevels',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['logLevel', 'localizedValue'],
    storeId: 'loglevels',

    proxy: {
        type: 'rest',
        url: '/api/mdc/field/logLevel',
        reader: {
            type: 'json',
            root: 'logLevels'
        }
    }
});
