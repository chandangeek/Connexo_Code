Ext.define('Imt.metrologyconfiguration.store.MetrologyConfiguration', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.MetrologyConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations/{id}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'metrologyconfigurations'
        }
    }
});