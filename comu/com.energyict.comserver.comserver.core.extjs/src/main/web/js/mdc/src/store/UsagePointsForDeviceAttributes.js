Ext.define('Mdc.store.UsagePointsForDeviceAttributes',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['id','mRID'],

    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints',
        reader: {
            type: 'json',
            root: 'usagePoints'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});