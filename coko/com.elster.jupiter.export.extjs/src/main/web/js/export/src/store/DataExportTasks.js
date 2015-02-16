Ext.define('Dxp.store.DataExportTasks', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DataExportTask',
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
