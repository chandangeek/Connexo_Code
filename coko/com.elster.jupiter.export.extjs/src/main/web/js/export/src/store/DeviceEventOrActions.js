Ext.define('Dxp.store.DeviceEventOrActions', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.EndDeviceEventTypePart',
    storeId: 'deviceEventOrActions',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/enddeviceeventtypes/deviceeventoractions',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'endDeviceEventTypePartInfos'
        }
    }
});