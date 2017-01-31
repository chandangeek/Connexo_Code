Ext.define('Imt.rulesets.store.EstimationRuleSetPurposes', {
    extend: 'Ext.data.Store',
    model: 'Imt.rulesets.model.MetrologyConfigurationPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/estimationruleset/{ruleSetId}/purposes',
        reader: {
            type: 'json',
            root: 'purposes'
        }
    }
});