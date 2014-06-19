Ext.define('Mdc.store.ConnectionMethodsOfDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceConnectionMethod'
    ],
    model: 'Mdc.model.DeviceConnectionMethod',
    storeId: 'ConnectionMethodsOfDevice',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mrid}/connectionmethods',
        reader: {
            type: 'json',
            root: 'connectionMethods'
        }
    }
});
