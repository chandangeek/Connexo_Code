Ext.define('Cfg.store.MetrologyConfigurations', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.UsagePointGroup'
    ],
    model: 'Cfg.model.UsagePointGroup',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/val/metrologyconfigurations',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metrologyConfigurations'
        }
    }
});
