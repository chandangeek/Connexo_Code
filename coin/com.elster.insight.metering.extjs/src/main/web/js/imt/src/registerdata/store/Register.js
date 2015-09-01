Ext.define('Imt.registerdata.store.Register', {
    extend: 'Ext.data.Store',
    model: 'Imt.registerdata.model.Register',
    data: [
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.1', readingTypeAlias: 'Bulk A- (kWh) 1', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 201},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.2', readingTypeAlias: 'Bulk A+ (kWh) 2', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 100202},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.3', readingTypeAlias: 'Bulk A- (kWh) 3', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 203},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.4', readingTypeAlias: 'Bulk A+ (kWh) 4', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 100204},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.5', readingTypeAlias: 'Bulk A- (kWh) 5', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 205},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.6', readingTypeAlias: 'Bulk A+ (kWh) 6', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 100206},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.7', readingTypeAlias: 'Bulk A- (kWh) 7', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 207},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.8', readingTypeAlias: 'Bulk A+ (kWh) 8', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 100208},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.9', readingTypeAlias: 'Bulk A- (kWh) 9', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 209},
        {deviceName: '999100E-name', readingTypemRID: '0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.10', readingTypeAlias: 'Bulk A+ (kWh) 10', utcTimestamp: 1440420305000, recordedTime: 1440421200000, readingValue: 100210},
   
    ],
    proxy: {
        type: 'rest',
        url: '/api/imt/usagepoints/{mrid}/registers',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterInfos'
        }
    }
});