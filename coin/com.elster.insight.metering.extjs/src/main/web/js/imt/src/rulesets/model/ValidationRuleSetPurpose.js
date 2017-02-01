Ext.define('Imt.rulesets.model.ValidationRuleSetPurpose', {
    extend: 'Imt.rulesets.model.MetrologyConfigurationPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationrulesets/{ruleSetId}/purposes',
        reader: {
            type: 'json'
        }
    }
});