Ext.define('Mdc.store.ComPortTypes',{
    extend: 'Ext.data.Store',
    fields: ['comPortType', 'localizedValue'],
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