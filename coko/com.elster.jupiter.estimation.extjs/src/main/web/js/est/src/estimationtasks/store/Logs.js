Ext.define('Est.estimationtasks.store.Logs', {
    extend: 'Ext.data.Store',
    model: 'Est.estimationtasks.model.Logs',
    pageSize: 50,
    proxy: {
        type: 'rest',
        //urlTpl: '/api/est/estimationtasks/{taskId}/history/{occurrenceId}',
        url: '/api/val/validationtasks/2/history/206686', // Test Url
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', params.taskId).replace('{occurrenceId}', params.occurrenceId);
        }
    }
});
