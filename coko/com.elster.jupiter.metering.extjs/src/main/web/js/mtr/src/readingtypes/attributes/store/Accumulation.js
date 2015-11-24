Ext.define('Mtr.readingtypes.attributes.store.Accumulation',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/accumulation',
        reader: {
            type: 'json',
            root: 'accumulationCodes'
        },
        limitParam: false
    }
});
