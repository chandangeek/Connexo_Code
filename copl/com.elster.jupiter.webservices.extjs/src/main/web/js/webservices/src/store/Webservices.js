Ext.define('Wss.store.Webservices', {
    extend: 'Ext.data.Store',
    model: 'Wss.model.Webservice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ws/webservices',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'webServices'
        }
    }
});
