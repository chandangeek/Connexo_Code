Ext.define('Mdc.store.DeviceConnectionLog', {
    extend: 'Ext.data.Store',
    storeId: 'deviceConnectionLog',
    requires: ['Mdc.model.DeviceConnectionLog'],
    model: 'Mdc.model.DeviceConnectionLog',
    pageSize: 10,

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mRID}/connectionmethods/{connectionId}/comsessions/{sessionId}',
        reader: {
            type: 'json',
            root: 'logs'
        }
    }
});