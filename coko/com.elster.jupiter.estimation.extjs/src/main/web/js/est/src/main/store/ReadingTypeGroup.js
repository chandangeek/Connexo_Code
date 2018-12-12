Ext.define('Est.main.store.ReadingTypeGroup', {
    extend: 'Ext.data.Store',
    model: 'Est.main.model.ReadingTypeGroup',
    storeId: 'ReadingTypeGroup',

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/readingtypegroup',
        reader: {
            type: 'json',
            root: 'readingTypeGroup'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});