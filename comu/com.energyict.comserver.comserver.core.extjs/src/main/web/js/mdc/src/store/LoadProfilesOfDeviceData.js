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
            this.url = this.urlTpl.replace('{mRID}', params.mRID).replace('{loadProfileId}', params.loadProfileId);
        }
    },
    setFilterModel: function (model) {
        var data = model.getData(),
            storeProxy = this.getProxy(),
            durationStore = Ext.getStore('Mdc.store.LoadProfileDataDurations'),
            duration = durationStore.getById(model.get('duration'));
        if (duration) {
            if (!Ext.isEmpty(data.intervalStart)) {
                storeProxy.setExtraParam('intervalStart', data.intervalStart.getTime());
                storeProxy.setExtraParam('intervalEnd', moment(data.intervalStart).add(duration.get('timeUnit'), duration.get('count')).valueOf());
                storeProxy.setExtraParam('onlySuspect', data.onlySuspect);
            }
        }
    }
});