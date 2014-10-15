Ext.define('Mdc.store.UnitOfMeasures',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['id','localizedValue'],
    storeId: 'unitOfMeasures',

    proxy: {
        type: 'rest',
        url: '/api/dtc/field/unitOfMeasure',
        reader: {
            type: 'json',
            root: 'units'
        }
    }
});