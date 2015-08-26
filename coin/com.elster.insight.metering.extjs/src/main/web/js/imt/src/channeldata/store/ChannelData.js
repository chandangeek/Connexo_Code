Ext.define('Imt.channeldata.store.ChannelData', {
    extend: 'Ext.data.Store',
    model: 'Imt.channeldata.model.ChannelData',
    data: [
        {interval: {start: 1440539100000, end: 1440540000000}, value: 930},
        {interval: {start: 1440538200000, end: 1440539100000}, value: 657},
        {interval: {start: 1440537300000, end: 1440538200000}, value: 1052},
        {interval: {start: 1440536400000, end: 1440537300000}, value: 903}
    ]
//    proxy: {
//        type: 'rest',
//        url: '/api/imt/channels/{id}',
//        timeout: 240000,
//        reader: {
//            type: 'json',
//            root: 'meterInfos'
//        }
//    }
});