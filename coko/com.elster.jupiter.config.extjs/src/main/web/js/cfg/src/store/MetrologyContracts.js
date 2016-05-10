Ext.define('Cfg.store.MetrologyContracts', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.UsagePointGroup'
    ],
    model: 'Cfg.model.UsagePointGroup',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/val/metrologyconfigurations/{configId}/contracts',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metrologyContracts'
        },

        setUrl: function (configId) {
            this.url = this.urlTpl.replace('{configId}', configId);
        }
    }
});
