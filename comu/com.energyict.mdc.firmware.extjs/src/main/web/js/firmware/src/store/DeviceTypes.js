Ext.define('Fwc.store.DeviceTypes', {
    extend: 'Ext.data.Store',
    model: 'Fwc.model.DeviceType',

    proxy: {
        type: 'rest',
        url: '/api/fwc/field/devicetypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json'
        }
    }
});