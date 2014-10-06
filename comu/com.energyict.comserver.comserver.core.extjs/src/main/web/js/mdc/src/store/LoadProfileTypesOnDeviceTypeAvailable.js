Ext.define('Mdc.store.LoadProfileTypesOnDeviceTypeAvailable', {
    extend: 'Ext.data.Store',
    //buffered: true,
   // pageSize: 20,
    requires: [
        'Mdc.model.LoadProfileType'
    ],
    model: 'Mdc.model.LoadProfileType',
    storeId: 'LoadProfileTypesOnDeviceTypeAvailable',
    //autoLoad: false,
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