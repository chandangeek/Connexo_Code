Ext.define('Mdc.store.DataLoggerSlaveRegisterHistory', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.DataLoggerSlaveRegisterHistory'
    ],

    model: 'Mdc.model.DataLoggerSlaveRegisterHistory',
    storeId: 'DataLoggerSlaveRegisterHistory',

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'registerHistory'
        },
        url: '/api/ddr/devices/{deviceId}/channels/{registerId}/history',
        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined
    }
});
