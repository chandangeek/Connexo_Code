Ext.define('Imt.rulesets.store.ValidationRuleSetPurposesToAdd', {
    extend: 'Ext.data.Store',
    model: 'Imt.rulesets.model.ValidationRuleSetPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationrulesets/{ruleSetId}/purposes/overview',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'purposes'
        }
    }
});