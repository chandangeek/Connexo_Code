Ext.define('Imt.channeldata.store.Channel', {
    extend: 'Ext.data.Store',
    model: 'Imt.channeldata.model.Channel',
    data: [
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.2', readingTypeAlias: 'Bulk A+ (kWh)', interval: {count: 15, timeUnit: "minutes"}},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A- (kWh)', interval: {count: 15, timeUnit: "minutes"}}

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