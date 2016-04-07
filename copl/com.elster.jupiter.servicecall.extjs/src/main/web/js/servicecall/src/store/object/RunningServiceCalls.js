Ext.define('Scs.store.object.RunningServiceCalls', {
    extend: 'Ext.data.Store',
    model: 'Scs.model.ServiceCall',
    autoLoad: false,
    proxy: {
        type: 'rest',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'serviceCalls'
        },
        setUrl: function (url) {
            this.url = url;
        }
    }


});
