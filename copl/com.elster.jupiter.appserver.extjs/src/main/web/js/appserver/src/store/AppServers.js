Ext.define('Apr.store.AppServers', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.AppServer',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/apr/appserver',
        reader: {
            type: 'json',
            root: 'appServers'
        }
    }
});
