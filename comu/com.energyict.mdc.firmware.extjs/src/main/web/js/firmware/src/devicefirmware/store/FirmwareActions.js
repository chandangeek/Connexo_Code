Ext.define('Fwc.devicefirmware.store.FirmwareActions', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.devicefirmware.model.FirmwareAction'
    ],
    model: 'Fwc.devicefirmware.model.FirmwareAction',
    storeId: 'DeviceFirmwareActions',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/device/{deviceId}/firmwaresactions',
        reader: {
            type: 'json',
            root: 'firmwareactions',
            totalProperty: 'total'
        },
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', encodeURIComponent(deviceId));
        }
    }
});
