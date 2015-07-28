Ext.define('Est.main.store.ReadingTypes', {
    extend: 'Ext.data.Store',
    model: 'Est.main.model.ReadingType',
    storeId: 'ReadingTypesToAddToEstimationRule',
    requires: [
        'Est.main.model.ReadingType'
    ],

    buffered: true,
    pageSize: 200,

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    }
});