
Ext.define('Mdc.store.ObisCodeFromReadingType', {
    extend: 'Ext.data.Store',
    fields: ['obisCode'],
    storeId: 'ObisCodeFromReadingType',
    proxy: {
        type: 'rest',
        url: '/api/mds/obiscode',
        reader: {
            type: 'json',
            root: 'obisValue'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});