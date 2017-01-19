Ext.define('Imt.metrologyconfiguration.model.EstimationRuleSet', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'name', 'inactiveRules', 'activeRules',
        {name: 'currentVersionId', persist: false},
        {name: 'metrologyContract', persist: false},
        {name: 'hiddenGroupId', persist: false},
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
