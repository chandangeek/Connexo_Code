Ext.define('Cfg.store.ReadingTypeGroup', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.ReadingTypeGroup',
    storeId: 'ReadingTypeGroup',
    requires: [
        'Cfg.model.ReadingTypeGroup'
    ],
    autoLoad: false,

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