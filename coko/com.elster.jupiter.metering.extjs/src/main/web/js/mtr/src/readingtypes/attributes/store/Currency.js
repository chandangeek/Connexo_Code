Ext.define('Mtr.readingtypes.attributes.store.Currency',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/currency',
        reader: {
            type: 'json',
            root: 'currencyCodes'
        },
        limitParam: false
    }
});
