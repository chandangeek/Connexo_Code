Ext.define('Imt.usagepointsetup.model.EffectiveMetrologyConfig', {
    extend: 'Imt.metrologyconfiguration.model.MetrologyConfiguration',
    fields: [
        {
            name: 'meterRoles', type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/metrologyconfiguration',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    }
});
