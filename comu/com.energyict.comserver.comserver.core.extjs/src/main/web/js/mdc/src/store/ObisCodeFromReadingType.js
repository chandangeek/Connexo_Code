
Ext.define('Mdc.store.ObisCodeFromReadingType', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ObisCode'
    ],
    model: 'Mdc.model.ObisCode',
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