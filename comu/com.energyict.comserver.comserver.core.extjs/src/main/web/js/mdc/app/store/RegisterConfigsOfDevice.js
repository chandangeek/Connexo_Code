Ext.define('Mdc.store.RegisterConfigsOfDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterConfiguration'
    ],
    model: 'Mdc.model.RegisterConfiguration',
    storeId: 'RegisterConfigsOfDevice',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
