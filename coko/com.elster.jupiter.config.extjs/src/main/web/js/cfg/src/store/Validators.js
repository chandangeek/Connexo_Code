Ext.define('Cfg.store.Validators', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.Validator',
    storeId: 'validators',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/val/validation/validators',
        reader: {
            type: 'json',
            root: 'validators'
        }
    }
});
