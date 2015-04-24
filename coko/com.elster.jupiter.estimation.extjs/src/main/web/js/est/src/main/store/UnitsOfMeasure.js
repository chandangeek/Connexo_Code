Ext.define('Est.main.store.UnitsOfMeasure', {
    extend: 'Ext.data.Store',
    model: 'Est.main.model.UnitOfMeasure',

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/unitsofmeasure',
        reader: {
            type: 'json',
            root: 'unitsOfMeasure'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});