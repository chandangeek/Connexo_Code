Ext.define('Mdc.store.LogLevels',{
    extend: 'Ext.data.Store',
    autoLoad: true,
    fields: ['logLevel'],
    storeId: 'loglevels',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/logLevel',
        reader: {
            type: 'json',
            root: 'logLevels'
        }
    }
});
