Ext.define('Mdc.store.DataLoggerSlaveChannelHistory', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.DataLoggerSlaveChannelHistory'
    ],

    model: 'Mdc.model.DataLoggerSlaveChannelHistory',
    storeId: 'DataLoggerSlaveChannelHistory',

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'channelHistory'
        },
        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,

        setUrl: function(mRID, channelId) {
            this.url = '/api/ddr/devices/' + encodeURIComponent(mRID) + '/channels/' + channelId + '/history'
        }
    }

});
