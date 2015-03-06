Ext.define('Cfg.store.DataValidationTasks', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.DataValidationTask',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/export/dataexporttask',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataExportTasks'
        }
    }
});
