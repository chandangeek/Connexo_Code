Ext.define('Wss.store.Endpoints', {
    extend: 'Ext.data.Store',
    model: 'Wss.model.Endpoint',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ws/endpointconfigurations',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'endpoints'
        }
    }
});
