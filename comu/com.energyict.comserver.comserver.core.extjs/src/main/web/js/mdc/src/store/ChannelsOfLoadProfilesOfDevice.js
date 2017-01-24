Ext.define('Mdc.store.ChannelsOfLoadProfilesOfDevice', {
    extend: 'Uni.data.store.Filterable',
    model: 'Mdc.model.ChannelOfLoadProfilesOfDevice',
    storeId: 'LoadProfilesOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/channels',
        reader: {
            type: 'json',
            root: 'channels',
            totalProperty: 'total'
        },
        timeout: 300000
    }
});