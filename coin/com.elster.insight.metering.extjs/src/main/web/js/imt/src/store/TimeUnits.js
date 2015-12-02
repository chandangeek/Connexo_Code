Ext.define('Imt.store.TimeUnits',{
    extend: 'Ext.data.Store',
    requires: [
        'Imt.model.field.TimeUnit'
    ],
    model: 'Imt.model.field.TimeUnit',
    storeId: 'TimeUnits',
    proxy: {
        type: 'rest',
        url: '/api/tmr/field/timeUnit',
        reader: {
            type: 'json',
            root: 'timeUnits'
        }
    }
});

