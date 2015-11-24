Ext.define('Mtr.readingtypes.attributes.store.ConsumptionTier',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/consumptiontier',
        reader: {
            type: 'json',
            root: 'consumptiontierCodes'
        },
        limitParam: false
    }
});
