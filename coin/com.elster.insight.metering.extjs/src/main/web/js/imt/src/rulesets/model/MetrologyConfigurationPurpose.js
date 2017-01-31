Ext.define('Imt.rulesets.model.MetrologyConfigurationPurpose', {
    extend: 'Ext.data.Model',
    fields: ['isActive', 'metrologyConfigurationInfo', 'purpose', 'outputs', 'metrologyContractId'],
    idProperty: 'metrologyContractId',
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationrulesets/{ruleSetId}/purposes',
        reader: {
            type: 'json'
        }
    }
});