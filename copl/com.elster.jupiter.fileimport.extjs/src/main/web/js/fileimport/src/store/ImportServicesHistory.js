Ext.define('Fim.store.ImportServicesHistory', {
    extend: 'Uni.data.store.Filterable',
    model: 'Fim.model.ImportServiceHistory',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/fir/importservices/{importServiceId}/history',
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (params) {
            if (params.importServiceId === undefined) {
                this.url = this.urlTpl.replace('/{importServiceId}', '');
            }
            else {
                this.url = this.urlTpl.replace('{importServiceId}', params.importServiceId);
            }
        }
    },
    listeners: {
        beforeload: function (store, operation, eOpts) {
            store.getProxy().setExtraParam('application', typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : null);
        }
    }

});
