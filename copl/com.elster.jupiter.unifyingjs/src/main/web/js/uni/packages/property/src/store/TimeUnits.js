Ext.define('Uni.property.store.TimeUnits', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.field.TimeUnit'
    ],
    model: 'Uni.property.model.field.TimeUnit',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/timeUnit',
        reader: {
            type: 'json',
            root: 'timeUnits'
        }
    }
});

