Ext.define('Cfg.model.ValidationRule', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'active',
        'action',
        'implementation'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Cfg.model.ValidationRuleProperty',
            associationKey: 'properties',
            name: 'properties'
        }
    ]
});

