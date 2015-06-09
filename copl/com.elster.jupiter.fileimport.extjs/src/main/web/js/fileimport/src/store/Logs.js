Ext.define('Fim.store.Logs', {
    extend: 'Uni.data.store.Filterable',

    model: 'Fim.model.Log',
    pageSize: 50,
    proxy: {
        type: 'rest',
        urlTpl: '/api/fir/importservices/history/{occurrenceId}/logs',
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{occurrenceId}', params.occurrenceId);
        }
    },
    listeners: {
        beforeload: function (store, operation, eOpts) {
            store.getProxy().setExtraParam('application', typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : null);
        }
    }

});
