Ext.define('Mdc.store.LoadProfileTypesOnDeviceType', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileType'
    ],
    model: 'Mdc.model.LoadProfileType',
    storeId: 'LoadProfileTypesOnDeviceType',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/loadprofiletypes',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});