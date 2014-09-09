Ext.define('Mdc.store.ChannelsOfLoadProfilesOfDevice', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ChannelOfLoadProfilesOfDevice',
    storeId: 'LoadProfilesOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/loadprofiles/{loadProfileId}/channels',
        reader: {
            type: 'json',
            root: 'channels'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', params.mRID).replace('{loadProfileId}', params.loadProfileId);
        }
    }
});