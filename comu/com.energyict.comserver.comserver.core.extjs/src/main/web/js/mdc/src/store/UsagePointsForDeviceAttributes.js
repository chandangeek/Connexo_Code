Ext.define('Mdc.store.UsagePointsForDeviceAttributes',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['id', 'mRID'],
    pageSize: 50,

    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints',
        reader: {
            type: 'json',
            root: 'usagePoints'
        },
        extraParams: {
            sort: 'mrid',
            dir: 'ASC'
        }
    }
});