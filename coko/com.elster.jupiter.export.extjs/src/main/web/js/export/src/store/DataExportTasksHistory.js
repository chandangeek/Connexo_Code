Ext.define('Dxp.store.DataExportTasksHistory', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DataExportTaskHistory',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/export/dataexporttask/{taskId}/history',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', params.taskId);
        }
    },
});
