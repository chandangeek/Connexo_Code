Ext.define('Cfg.store.DataValidationTasksHistory', {
    extend: 'Uni.data.store.Filterable',
    model: 'Cfg.model.DataValidationTaskHistory',
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
    }
});
