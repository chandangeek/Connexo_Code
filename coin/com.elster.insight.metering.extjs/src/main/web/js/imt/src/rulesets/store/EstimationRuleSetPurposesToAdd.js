Ext.define('Imt.rulesets.store.EstimationRuleSetPurposesToAdd', {
    extend: 'Ext.data.Store',
    model: 'Imt.rulesets.model.EstimationRuleSetPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/estimationrulesets/{ruleSetId}/purposes/overview',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'purposes'
        }
    }
});