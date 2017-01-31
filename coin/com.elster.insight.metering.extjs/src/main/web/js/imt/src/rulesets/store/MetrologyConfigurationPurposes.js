Ext.define('Imt.rulesets.store.MetrologyConfigurationPurposes', {
    extend: 'Ext.data.Store',
    model: 'Imt.rulesets.model.MetrologyConfigurationPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationruleset/{ruleSetId}/purposes',
        reader: {
            type: 'json',
            root: 'purposes'
        }
    }
});