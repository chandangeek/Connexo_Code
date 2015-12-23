Ext.define('Imt.channeldata.model.ChannelDataFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        {name: 'intervalStart', type: 'date'},
        {name: 'duration'},
        {name: 'onlySuspect'},
        {name: 'onlyNonSuspect'}
    ]
});