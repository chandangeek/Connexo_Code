Ext.define('Imt.rulesets.model.MetrologyConfigurationPurpose', {
    extend: 'Uni.model.Version',
    fields: ['active', 'name', 'metrologyConfigurationInfo', 'outputs'],
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationrulesets/{ruleSetId}/purposes',
        reader: {
            type: 'json'
        }
    }
});