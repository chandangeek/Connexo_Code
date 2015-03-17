Ext.define('Cfg.store.DataValidationTasks', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.DataValidationTask',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/val/datavalidationtasks',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataValidationTasks'
        }
    }
});
