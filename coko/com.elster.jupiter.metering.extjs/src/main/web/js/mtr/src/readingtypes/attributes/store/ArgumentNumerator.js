Ext.define('Mtr.readingtypes.attributes.store.ArgumentNumerator',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/argumentnumerator',
        reader: {
            type: 'json',
            root: 'argumentnumeratorCodes'
        },
        limitParam: false
    }
});
