Ext.define('Imt.rulesets.model.EstimationRuleSetPurpose', {
    extend: 'Imt.rulesets.model.MetrologyConfigurationPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/estimationrulesets/{ruleSetId}/purposes',
        reader: {
            type: 'json'
        }
    }
});