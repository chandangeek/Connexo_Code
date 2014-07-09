Ext.define('Mdc.store.RegisterConfigsOfDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Register'
    ],
    model: 'Mdc.model.Register',
    storeId: 'RegisterConfigsOfDevice',
    pageSize: 10,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
