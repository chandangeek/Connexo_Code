Ext.define('Imt.channeldata.store.ChannelData', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Imt.channeldata.store.ChannelDataDurations'
    ],
    model: 'Imt.channeldata.model.ChannelData',
    storeId: 'channelData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/channels/{channelId}/data',
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
            durationStore = Ext.getStore('Imt.channeldata.store.ChannelDataDurations'),
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