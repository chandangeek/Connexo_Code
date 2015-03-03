Ext.define('Mdc.store.ChannelsOfLoadProfilesOfDevice', {
    extend: 'Uni.data.store.Filterable',
    model: 'Mdc.model.ChannelOfLoadProfilesOfDevice',
    storeId: 'LoadProfilesOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/channels',
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