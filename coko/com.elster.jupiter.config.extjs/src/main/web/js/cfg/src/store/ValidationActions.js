Ext.define('Cfg.store.ValidationActions', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Cfg.model.ValidationAction',
    storeId: 'validationActions',

    proxy: {
        type: 'rest',
        url: '/api/val/validation/actions',
        reader: {
            type: 'json',
            root: 'actions'
        }
    }
})
