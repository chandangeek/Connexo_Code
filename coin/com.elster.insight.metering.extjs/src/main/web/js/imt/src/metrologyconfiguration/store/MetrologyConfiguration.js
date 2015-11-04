Ext.define('Imt.metrologyconfiguration.store.MetrologyConfiguration', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.MetrologyConfiguration',
    proxy: {
        type: 'rest',
        urlTpl: '/api/ucr/metrologyconfigurations/{id}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'metrologyconfigurations'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', encodeURIComponent(params));
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});