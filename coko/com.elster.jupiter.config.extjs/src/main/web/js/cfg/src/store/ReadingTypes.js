Ext.define('Cfg.store.ReadingTypes', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Cfg.model.ReadingType',
    storeId: 'readingTypes'

    /*proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    }   */
})
