Ext.define('Uni.store.UnitsOfMeasure', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.UnitOfMeasure',

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