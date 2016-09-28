Ext.define('Mdc.store.DeviceConnectionHistory', {
    extend: 'Ext.data.Store',
    storeId: 'DeviceConnectionHistory',
    requires: ['Mdc.model.DeviceConnectionHistory'],
    model: 'Mdc.model.DeviceConnectionHistory',
    pageSize: 10,

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connectionmethods/{connectionId}/comsessions',
        reader: {
            type: 'json',
            root: 'comSessions'
        }
    }
});
