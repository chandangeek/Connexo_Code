Ext.define('Mdc.store.ComPortTypes',{
    autoLoad: true,
    fields: ['comPortType'],
    storeId: 'comporttypes',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/comPortType',
        reader: {
            type: 'json',
            root: 'comPortTypes'
        }
    }
});