Ext.define('Imt.rulesets.store.MetrologyConfigurationPurposesToAdd', {
    extend: 'Ext.data.Store',
    model: 'Imt.rulesets.model.MetrologyConfigurationPurpose',
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