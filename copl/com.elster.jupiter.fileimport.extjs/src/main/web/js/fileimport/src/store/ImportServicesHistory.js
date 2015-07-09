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
    }

});
