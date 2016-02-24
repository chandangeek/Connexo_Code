Ext.define('Scs.store.ServiceCalls', {
    extend: 'Ext.data.Store',
    model: 'Scs.model.ServiceCall',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/scs/servicecalls',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'serviceCalls'
        }
    }
});
