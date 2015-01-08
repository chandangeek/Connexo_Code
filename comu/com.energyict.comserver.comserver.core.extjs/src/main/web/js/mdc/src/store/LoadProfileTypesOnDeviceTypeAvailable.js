Ext.define('Mdc.store.LoadProfileTypesOnDeviceTypeAvailable', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileType'
    ],
    model: 'Mdc.model.LoadProfileType',
    storeId: 'LoadProfileTypesOnDeviceTypeAvailable',
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/loadprofiletypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'data'
        },
        extraParams: {
            available: true
        }
    }
});