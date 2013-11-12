Ext.define('Mdc.store.TimeUnits',{
    requires: [
        'Mdc.model.TimeUnit'
    ],
    model: 'Mdc.model.TimeUnit',
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

