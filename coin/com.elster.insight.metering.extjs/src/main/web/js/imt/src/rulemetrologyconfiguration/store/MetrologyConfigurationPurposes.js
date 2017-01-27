Ext.define('Imt.rulemetrologyconfiguration.store.MetrologyConfigurationPurposes', {
    extend: 'Ext.data.Store',
    model: 'Imt.rulemetrologyconfiguration.model.MetrologyConfigurationPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationruleset/{ruleSetId}/purposes',
        reader: {
            type: 'json',
            root: 'purposes'
        }
    }
});