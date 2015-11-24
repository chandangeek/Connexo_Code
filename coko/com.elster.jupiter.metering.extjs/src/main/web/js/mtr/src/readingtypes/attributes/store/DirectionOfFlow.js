Ext.define('Mtr.readingtypes.attributes.store.DirectionOfFlow',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/flowdirection',
        reader: {
            type: 'json',
            root: 'flowdirectionCodes'
        },
        limitParam: false
    }
});

