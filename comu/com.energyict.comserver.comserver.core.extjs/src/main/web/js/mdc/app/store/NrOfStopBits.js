Ext.define('Mdc.store.NrOfStopBits',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.NrOfStopBits'
    ],
    model: 'Mdc.model.field.NrOfStopBits',
    autoLoad: true,
    storeId: 'NrOfStopBits',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/nrOfStopBits',
        reader: {
            type: 'json',
            root: 'nrOfStopBits'
        }
    }
});

