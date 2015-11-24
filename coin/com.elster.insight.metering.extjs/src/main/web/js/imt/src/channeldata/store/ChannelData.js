Ext.define('Imt.channeldata.store.ChannelData', {
    extend: 'Ext.data.Store',
    model: 'Imt.channeldata.model.ChannelData',
    proxy: {
        type: 'rest',
//        url: 'http://localhost:8080/apps/imt/src/store/sample.json',
        urlTpl: '/api/udr/usagepoints/{mRID}/channels/{channelId}/data',
        timeout: 480000,
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID)).replace('{channelId}', params.channelId);
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});