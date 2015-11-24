Ext.define('Mtr.readingtypes.attributes.store.Commodity',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/commodity',
        reader: {
            type: 'json',
            root: 'commodityCodes'
        },
        limitParam: false
    }
});

