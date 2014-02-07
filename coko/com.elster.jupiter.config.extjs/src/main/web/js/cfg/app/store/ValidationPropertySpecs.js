Ext.define('Cfg.store.ValidationPropertySpecs', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Cfg.model.ValidationPropertySpec',
    storeId: 'validationPropertySpec',

    proxy: {
        type: 'rest',
        url: '/api/val/validation/propertyspecs',
        reader: {
            type: 'json',
            root: 'propertySpecs'
        }
    }
})
