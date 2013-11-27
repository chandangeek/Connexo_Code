Ext.define('Cfg.store.AvailableReadingTypes', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Cfg.model.ReadingType',
    storeId: 'availableReadingTypes',

    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    }
})
