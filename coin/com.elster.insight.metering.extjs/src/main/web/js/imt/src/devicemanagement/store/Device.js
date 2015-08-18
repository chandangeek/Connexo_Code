Ext.define('Imt.devicemanagement.store.Device', {
    extend: 'Ext.data.Store',
    model: 'Imt.devicemanagement.model.Device',
    proxy: {
        type: 'rest',
        url: '/api/imt/devices/{mRID}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterInfos'
        }
    }
});