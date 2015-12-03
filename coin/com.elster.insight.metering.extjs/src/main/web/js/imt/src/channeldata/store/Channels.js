Ext.define('Imt.channeldata.store.Channels', {
    extend: 'Uni.data.store.Filterable',
    model: 'Imt.channeldata.model.Channel',
    storeId: 'channels',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/channels',
        reader: {
            type: 'json',
            root: 'channels',
            totalProperty: 'total'
        },
        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (mRID) {
                this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID))
        }
    }
});