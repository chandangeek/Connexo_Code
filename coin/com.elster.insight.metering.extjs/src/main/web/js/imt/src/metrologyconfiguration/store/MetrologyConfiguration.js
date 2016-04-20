Ext.define('Imt.metrologyconfiguration.store.MetrologyConfiguration', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.MetrologyConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations',
        reader: {
            type: 'json',
            root: 'metrologyconfigurations'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});