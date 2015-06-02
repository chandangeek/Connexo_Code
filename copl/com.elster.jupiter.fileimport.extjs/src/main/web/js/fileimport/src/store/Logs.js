Ext.define('Fim.store.Logs', {
    extend: 'Uni.data.store.Filterable',

    model: 'Fim.model.Log',
    pageSize: 50,
    proxy: {
        type: 'rest',
        urlTpl: '/api/val/validationtasks/{taskId}/history/{occurrenceId}',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', 1/*params.taskId*/).replace('{occurrenceId}', 38361/*params.occurrenceId*/);
        }
    },
    listeners: {
        beforeload: function (store, operation, eOpts) {
            store.getProxy().setExtraParam('application', typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : null);
        }
    }

});
