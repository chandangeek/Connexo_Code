Ext.define('Imt.rulesets.model.MetrologyConfigurationPurpose', {
    extend: 'Ext.data.Model',
    fields: ['isActive', 'metrologyConfigurationInfo', 'outputs', 'metrologyContractInfo',
        {name: 'id', mapping: 'metrologyContractInfo.id'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationrulesets/{ruleSetId}/purposes',
        reader: {
            type: 'json'
        }
    }
});