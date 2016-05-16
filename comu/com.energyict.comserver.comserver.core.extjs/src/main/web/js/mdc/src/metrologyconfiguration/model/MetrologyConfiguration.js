Ext.define('Mdc.metrologyconfiguration.model.MetrologyConfiguration', {
    extend: 'Uni.model.Version',
    fields: ['id',
        'name',
        'description',
        {name: 'status', defaultValue: null},
        'serviceCategory',
        'readingTypes'
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/metrologyconfigurations',
        reader: {
            type: 'json'
        }
    }
});