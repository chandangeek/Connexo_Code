Ext.define('Mdc.store.TimeUnits',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.TimeUnit'
    ],
    model: 'Mdc.model.field.TimeUnit',
    autoLoad: true,
    storeId: 'TimeUnits',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/timeUnit',
        reader: {
            type: 'json',
            root: 'timeUnits'
        }
    }
});

