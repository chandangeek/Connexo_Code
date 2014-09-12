Ext.define('Mdc.store.NrOfDataBits',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.NrOfDataBits'
    ],
    model: 'Mdc.model.field.NrOfDataBits',
    autoLoad: true,
    storeId: 'NrOfDataBits',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/nrOfDataBits',
        reader: {
            type: 'json',
            root: 'nrOfDataBits'
        }
    }
});

