Ext.define('Mdc.store.ChannelOfLoadProfileOfDeviceData', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ChannelOfLoadProfileOfDeviceData',
    storeId: 'ChannelOfLoadProfileOfDeviceData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/loadprofiles/{loadProfileId}/channels/{channelId}/data',
        reader: {
            type: 'json',
            root: 'data'
        },

        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', params.mRID).replace('{loadProfileId}', params.loadProfileId).replace('{channelId}', params.channelId);
        }
    }
});