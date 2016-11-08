Ext.define('Mdc.store.ConnectionMethodsOfDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceConnectionMethod'
    ],
    model: 'Mdc.model.DeviceConnectionMethod',
    storeId: 'ConnectionMethodsOfDevice',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connectionmethods',
        reader: {
            type: 'json',
            root: 'connectionMethods'
        }
    }
});
