Ext.define('Mtr.readingtypes.attributes.store.Phase',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/phases',
        reader: {
            type: 'json',
            root: 'phasesCodes'
        },
        limitParam: false
    }
});
