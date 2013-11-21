Ext.define('Cfg.store.ValidationActions', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Cfg.model.ValidationAction',

    proxy: {
        type: 'rest',
        url: '/api/val/validation/actions',
        reader: {
            type: 'json',
            root: 'actions'
        }
    }
})
