Ext.define('Mdc.usagepointmanagement.store.ChannelData', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.ChannelReading',
    proxy: {
        type: 'rest',
        urlTpl: '/api/upr/usagepoints/{mRID}/channels/{channelId}/data',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (mRID, channelId) {
            this.url = this.urlTpl.replace('{mRID}', mRID)
                .replace('{channelId}', channelId);
        }
    }
});