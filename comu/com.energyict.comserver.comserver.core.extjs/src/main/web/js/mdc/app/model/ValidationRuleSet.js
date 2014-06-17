Ext.define('Mdc.model.ValidationRuleSet', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'description',
        'numberOfInactiveRules',
        'numberOfRules'
    ]
});