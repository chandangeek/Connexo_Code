Ext.define('Imt.validationrulesets.store.MetrologyConfigurationPurposesToAdd', {
    extend: 'Ext.data.Store',
    model: 'Imt.validationrulesets.model.MetrologyConfigurationPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationruleset/{ruleSetId}/purposes/overview',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'purposes'
        }
    }
});