Ext.define('Mdc.store.LoadProfilesOfDeviceData', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Mdc.store.LoadProfileDataDurations'
    ],
    model: 'Mdc.model.LoadProfilesOfDeviceData',
    storeId: 'LoadProfilesOfDeviceData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/loadprofiles/{loadProfileId}/data',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID)).replace('{loadProfileId}', params.loadProfileId);
        }
    }
});