Ext.define('Fwc.store.FirmwareTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.model.FirmwareType'
    ],
    model: 'Fwc.model.FirmwareType',
    storeId: 'FirmwareTypes',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fwc/field/firmwareTypes',
        reader: {
            type: 'json',
            root: 'firmwareTypes'
        }
    }
});
