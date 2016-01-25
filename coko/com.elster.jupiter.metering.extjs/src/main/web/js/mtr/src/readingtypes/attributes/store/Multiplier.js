Ext.define('Mtr.readingtypes.attributes.store.Multiplier',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/metricMultiplier',
        reader: {
            type: 'json',
            root: 'metricMultiplierCodes'
        },
        limitParam: false
    }
});
