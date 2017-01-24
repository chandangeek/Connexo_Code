Ext.define('Mdc.usagepointmanagement.store.ChannelData', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.ChannelReading',
    proxy: {
        type: 'rest',
        url: '/api/upr/usagepoints/{usagePointId}/channels/{channelId}/data',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});