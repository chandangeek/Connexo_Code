Ext.define('Imt.validationrulesets.model.MetrologyConfigurationPurpose', {
    extend: 'Ext.data.Model',
    fields: ['isActive', 'metrologyConfigurationInfo', 'purpose', 'outputs', 'metrologyContractId'],
    idProperty: 'metrologyContractId',
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationruleset/{ruleSetId}/purposes',
        reader: {
            type: 'json'
        }
    }
});