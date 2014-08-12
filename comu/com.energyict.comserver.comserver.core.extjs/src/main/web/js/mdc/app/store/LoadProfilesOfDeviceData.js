Ext.define('Mdc.store.LoadProfilesOfDeviceData', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.LoadProfilesOfDeviceData',
    storeId: 'LoadProfilesOfDeviceData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/loadprofiles/{loadProfileId}/data',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});