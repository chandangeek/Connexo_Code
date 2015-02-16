Ext.define('Cfg.store.ReadingTypesToAddForRule', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.ReadingType',
    storeId: 'ReadingTypesToAddForRule',
    requires: [
        'Cfg.model.ReadingType'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        },
        limitParam: undefined,
        startParam: undefined,
        pageParam: undefined
    }
});