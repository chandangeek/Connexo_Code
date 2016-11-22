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
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/history',
        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined
    }
});
