Ext.define('Cfg.model.ValidationRuleSet', {
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

    /*associations: [
     {
     type: 'hasMany',
     model: 'Cfg.model.ValidationRule',
     associationKey: 'rules',
     name: 'rules'
     }
     ], */

    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        headers: {'Accept': 'application/json'},
        reader: {
            type: 'json'
        }
    }
});
