Ext.define('Mtr.readingtypes.attributes.store.Kind',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/measurementkind',
        reader: {
            type: 'json',
            root: 'measurementkindCodes'
        },
        limitParam: false
    }
});
