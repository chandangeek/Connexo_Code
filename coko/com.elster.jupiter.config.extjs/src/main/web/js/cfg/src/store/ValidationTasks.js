Ext.define('Cfg.store.ValidationTasks', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.ValidationTask',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/val/validationtasks',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataValidationTasks'
        }
    }
});
