Ext.define('Imt.registerdata.store.Reading', {
    extend: 'Ext.data.Store',
    model: 'Imt.registerdata.model.Reading',
    data: [
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1438473600000, recordedTime: 1438488000000, readingValue: 100001},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1438560000000, recordedTime: 1438574400000, readingValue: 100101},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1438646400000, recordedTime: 1438660800000, readingValue: 100201},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1438732800000, recordedTime: 1438747200000, readingValue: 100301},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1438819200000, recordedTime: 1438833600000, readingValue: 100401},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1438905600000, recordedTime: 1438920000000, readingValue: 100501},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1438992000000, recordedTime: 1439006400000, readingValue: 100601},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1439078400000, recordedTime: 1439092800000, readingValue: 100701},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1439164800000, recordedTime: 1439179200000, readingValue: 100801},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A+ (kWh) 1', utcTimestamp: 1439251200000, recordedTime: 1439265600000, readingValue: 100901},

    ],
    proxy: {
        type: 'rest',
        url: '/api/imt/usagepoints/{mrid}/registers/{mrid}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterInfos'
        }
    }
});

