Ext.define('Imt.metrologyconfiguration.model.EstimationRuleSet', {
    extend: 'Uni.model.Version',
    fields: [
        'id', 'name',
        {name: 'currentVersionId', persist: false},
        {name: 'metrologyContract', persist: false},
        {name: 'metrologyContractIsMandatory', persist: false},
        {name: 'metrologyContractId', persist: false},
        {name: 'noRuleSets', persist: false, defaultValue: false},
        {
            name: 'uniqueId',
            persist: false,
            type: 'string'
        }
    ],
    idProperty: 'uniqueId'
});
