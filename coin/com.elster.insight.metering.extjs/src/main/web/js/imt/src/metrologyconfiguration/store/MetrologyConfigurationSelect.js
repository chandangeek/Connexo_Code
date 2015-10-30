Ext.define('Imt.metrologyconfiguration.store.MetrologyConfigurationSelect', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.MetrologyConfiguration',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'metrologyconfigurations'
        }
    },
    listeners: {
        load: function(store, records) {            
            store.insert(0, [{id:0, name:'NONE'}]);
        }
    }
});