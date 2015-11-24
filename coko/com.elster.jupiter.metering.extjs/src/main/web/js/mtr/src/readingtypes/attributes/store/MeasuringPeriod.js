Ext.define('Mtr.readingtypes.attributes.store.MeasuringPeriod',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/measuringperiod',
        reader: {
            type: 'json',
            root: 'measuringperiodCodes'
        },
        limitParam: false
    }
});

