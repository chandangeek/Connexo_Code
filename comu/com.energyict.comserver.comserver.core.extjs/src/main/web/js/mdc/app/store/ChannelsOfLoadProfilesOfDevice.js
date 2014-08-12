Ext.define('Mdc.store.ChannelsOfLoadProfilesOfDevice', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ChannelOfLoadProfilesOfDevice',
    storeId: 'LoadProfilesOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/loadprofiles/{loadProfileId}/channels',
        reader: {
            type: 'json',
            root: 'channels'
        }
    }
});