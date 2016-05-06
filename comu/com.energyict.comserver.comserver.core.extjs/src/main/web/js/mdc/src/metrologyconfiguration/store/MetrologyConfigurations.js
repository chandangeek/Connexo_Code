Ext.define('Mdc.metrologyconfiguration.store.MetrologyConfigurations', {
    extend: 'Ext.data.Store',
    model: 'Mdc.metrologyconfiguration.model.MetrologyConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/mtr/metrologyconfigurations',
        reader: {
            type: 'json',
            root: 'metrologyConfigurations'
        }
    }
});