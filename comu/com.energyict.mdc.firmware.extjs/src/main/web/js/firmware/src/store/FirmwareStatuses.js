Ext.define('Fwc.store.FirmwareStatuses', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.model.FirmwareStatus'
    ],
    model: 'Fwc.model.FirmwareStatus',
    storeId: 'FirmwareStatuses',
    autoLoad: false,
    remoteFilter: false,
    proxy: {
        type: 'rest',
        url: '/api/fwc/field/firmwareStatuses',
        reader: {
            type: 'json',
            root: 'firmwareStatuses'
        }
    }
});
