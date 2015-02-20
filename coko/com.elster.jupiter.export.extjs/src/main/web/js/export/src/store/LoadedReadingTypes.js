Ext.define('Dxp.store.LoadedReadingTypes', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.ReadingTypeFullData',
    storeId: 'LoadedReadingTypes',
    requires: [
        'Dxp.model.ReadingTypeFullData'
    ],

    autoLoad: false,

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