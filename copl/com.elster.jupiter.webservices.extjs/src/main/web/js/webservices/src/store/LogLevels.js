Ext.define('Wss.store.LogLevels', {
    extend: 'Ext.data.Store',
    model: 'Wss.model.LogLevel',
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