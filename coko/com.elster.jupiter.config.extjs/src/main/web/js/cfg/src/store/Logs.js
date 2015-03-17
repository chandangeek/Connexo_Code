Ext.define('Cfg.store.Logs', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.Log',
    pageSize: 50,
    proxy: {
        type: 'rest',
        urlTpl: '/api/val/datavalidationtasks/{taskId}/history/{occurrenceId}',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', params.taskId).replace('{occurrenceId}', params.occurrenceId);
        }
    }
});
