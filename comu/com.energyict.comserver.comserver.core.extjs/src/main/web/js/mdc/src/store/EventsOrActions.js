Ext.define('Mdc.store.EventsOrActions',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.EventOrAction'
    ],
    model: 'Mdc.model.EventOrAction',
    storeId: 'EventsOrActions',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/api/ddr/field/enddeviceeventoractions',
        reader: {
            type: 'json',
            root: 'eventOrActions'
        }
    }
});
