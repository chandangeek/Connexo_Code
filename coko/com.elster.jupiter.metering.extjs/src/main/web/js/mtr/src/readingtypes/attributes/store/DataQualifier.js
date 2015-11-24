Ext.define('Mtr.readingtypes.attributes.store.DataQualifier',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/aggregate',
        reader: {
            type: 'json',
            root: 'aggregateCodes'
        },
        limitParam: false
    }
});
