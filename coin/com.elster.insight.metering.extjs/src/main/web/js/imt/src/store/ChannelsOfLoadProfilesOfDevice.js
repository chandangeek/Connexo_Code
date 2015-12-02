Ext.define('Imt.store.ChannelsOfLoadProfilesOfDevice', {
    extend: 'Uni.data.store.Filterable',
    model: 'Imt.model.ChannelOfLoadProfilesOfDevice',
    storeId: 'LoadProfilesOfDevice',
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