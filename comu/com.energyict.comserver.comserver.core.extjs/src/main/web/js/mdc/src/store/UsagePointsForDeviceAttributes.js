Ext.define('Mdc.store.UsagePointsForDeviceAttributes',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'mRID', type: 'string'}
    ],
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