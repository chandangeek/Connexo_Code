Ext.define('Mdc.store.LogLevels',{
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
