Ext.define('Imt.channeldata.store.Channel', {
    extend: 'Ext.data.Store',
    model: 'Imt.channeldata.model.Channel',
    data: [
        {id: 1, name: 'kWh Delivered'},
        {id: 2, name: 'kWh Received'}
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