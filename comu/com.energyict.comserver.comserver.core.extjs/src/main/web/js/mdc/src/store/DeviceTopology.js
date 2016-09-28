Ext.define('Mdc.store.DeviceTopology', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'DeviceTopology',

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'slaveDevices'
        },
        url: '/api/ddr/devices/{deviceId}/topology/communication'
    }
});
