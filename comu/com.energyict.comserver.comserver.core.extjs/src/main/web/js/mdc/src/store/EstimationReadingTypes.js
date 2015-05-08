Ext.define('Mdc.store.EstimationReadingTypes', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.EstimationReadingType',

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
