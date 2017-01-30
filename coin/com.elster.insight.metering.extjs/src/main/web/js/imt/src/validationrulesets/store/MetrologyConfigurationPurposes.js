Ext.define('Imt.validationrulesets.store.MetrologyConfigurationPurposes', {
    extend: 'Ext.data.Store',
    model: 'Imt.validationrulesets.model.MetrologyConfigurationPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationruleset/{ruleSetId}/purposes',
        reader: {
            type: 'json',
            root: 'purposes'
        }
    }
});