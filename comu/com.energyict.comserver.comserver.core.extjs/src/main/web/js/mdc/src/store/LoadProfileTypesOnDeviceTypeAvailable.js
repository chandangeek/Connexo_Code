Ext.define('Mdc.store.LoadProfileTypesOnDeviceTypeAvailable', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileType'
    ],
    model: 'Mdc.model.LoadProfileType',
    storeId: 'LoadProfileTypesOnDeviceTypeAvailable',

    buffered: true,
    pageSize: 200,

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/loadprofiletypes',
        reader: {
            type: 'json',
            root: 'data'
        },
        extraParams: {
            available: true
        }
    }
});