Ext.define('Wss.store.LogLevels', {
    extend: 'Ext.data.Store',
    fields: [{name:'logLevel'},{name:'localizedValue'}],
    proxy: {
        type: 'rest',
        url: '/api/ws/fields/logLevel',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'logLevels'
        }
    }
});