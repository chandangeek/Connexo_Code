Ext.define('Fwc.devicefirmware.store.Firmwares', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.devicefirmware.model.Firmware'
    ],
    model: 'Fwc.devicefirmware.model.Firmware',
    storeId: 'DeviceFirmwares',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/device/{deviceId}/firmwares',
        reader: {
            type: 'json',
            root: 'firmwares',
            totalProperty: 'total'
        },
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', encodeURIComponent(deviceId));
        }
    }
});
