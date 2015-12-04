Ext.define('Imt.channeldata.model.ChannelsFilter', {
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
        { name: 'id', type: 'auto', persist: false }
    ]
});
