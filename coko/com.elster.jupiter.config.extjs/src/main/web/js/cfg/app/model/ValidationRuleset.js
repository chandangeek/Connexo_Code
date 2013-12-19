Ext.define('Cfg.model.ValidationRuleSet', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'description',
        'numberOfActiveRules',
        'numberOfRules'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Cfg.model.ValidationRule',
            associationKey: 'rules',
            name: 'rules'
        }
    ]


});
