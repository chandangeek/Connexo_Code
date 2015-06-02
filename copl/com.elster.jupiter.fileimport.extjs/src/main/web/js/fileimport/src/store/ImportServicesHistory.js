Ext.define('Fim.store.ImportServicesHistory', {
    extend: 'Uni.data.store.Filterable',
    model: 'Fim.model.ImportServiceHistory',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/val/validationtasks/{taskId}/history',
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{taskId}', 1/*params.importServiceId*/);
        }
    },
    listeners: {
        beforeload: function (store, operation, eOpts) {
            store.getProxy().setExtraParam('application', typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : null);
        }
    }

});
