Ext.define('Mtr.readingtypes.attributes.store.InterharmonicNumerator',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/interharmonicnumerator',
        reader: {
            type: 'json',
            root: 'interharmonicnumeratorCodes'
        },
        limitParam: false
    }
});
