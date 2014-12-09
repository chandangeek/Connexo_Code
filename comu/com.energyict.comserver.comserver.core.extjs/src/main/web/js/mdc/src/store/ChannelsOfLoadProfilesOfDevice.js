Ext.define('Mdc.store.ChannelsOfLoadProfilesOfDevice', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ChannelOfLoadProfilesOfDevice',
    storeId: 'LoadProfilesOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/channels',
        reader: {
            type: 'json',
            root: 'channels'
        },

        setUrl: function (mRID) {
                this.url = this.urlTpl.replace('{mRID}', mRID)
        }
    }
});