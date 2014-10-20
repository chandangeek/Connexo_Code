Ext.define('Mdc.store.DeviceConnectionLog', {
    extend: 'Uni.data.store.Filterable',
    storeId: 'deviceConnectionLog',
    requires: ['Mdc.model.DeviceConnectionLog'],
    model: 'Mdc.model.DeviceConnectionLog',
//    pageSize: 20,
//    buffered: true,
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mRID}/connectionmethods/{connectionId}/comsessions/{sessionId}/journals',
        timeout: 250000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'journals'
        }
    }
});