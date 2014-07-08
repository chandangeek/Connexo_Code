Ext.define('Mdc.model.DeviceConfigurationValidationRuleSet', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'description',
        'numberOfInactiveRules',
        'numberOfRules',
        {
            name: 'active_rules',
            persist: false,
            mapping: function (data) {
                return data.numberOfRules - data.numberOfInactiveRules;
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        headers: {'Accept': 'application/json'},
        reader: {
            type: 'json'
        }
    }
});

