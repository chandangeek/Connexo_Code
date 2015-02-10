Ext.define('Apr.store.AppServers', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.AppServer',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/apr/appserver',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'appServers'
        }
    }
});
