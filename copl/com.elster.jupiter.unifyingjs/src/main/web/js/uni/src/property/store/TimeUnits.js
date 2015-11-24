Ext.define('Uni.property.store.TimeUnits', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.field.TimeUnit'
    ],
    model: 'Uni.property.model.field.TimeUnit',
    proxy: {
        type: 'rest',
        url: '/api/tmr/field/timeUnit',
        reader: {
            type: 'json',
            root: 'timeUnits'
        }
    }
});

