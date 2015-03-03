Ext.define('Mdc.store.ChannelOfLoadProfileOfDeviceData', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Mdc.store.LoadProfileDataDurations'
    ],
    model: 'Mdc.model.ChannelOfLoadProfileOfDeviceData',
    storeId: 'ChannelOfLoadProfileOfDeviceData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/channels/{channelId}/data',
        reader: {
            type: 'json',
            root: 'data'
        },

        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID)).replace('{channelId}', params.channelId);
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
                storeProxy.setExtraParam('onlyNonSuspect', data.onlyNonSuspect);
            }
        }
    }
});