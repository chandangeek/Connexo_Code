Ext.define('Mdc.store.RegisterData', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterData',
        'Mdc.store.RegisterDataDurations'
    ],

    model: 'Mdc.model.RegisterData',
    storeId: 'RegisterData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers/{registerId}/data',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});