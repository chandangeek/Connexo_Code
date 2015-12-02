Ext.define('Imt.model.filter.DeviceChannelsFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'channelName', type: 'auto' },
        { name: 'loadProfileName', type: 'auto' },
        { name: 'id', type: 'auto', persist: false }
    ]
});
