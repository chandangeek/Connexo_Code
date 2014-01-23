Ext.define('Cfg.model.ValidationRule', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'active',
        'implementation',
        'displayName',
        'readingTypes',
        'properties'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Cfg.model.ValidationRuleProperty',
            associationKey: 'properties',
            name: 'properties'
        },
        {
            type: 'hasMany',
            model: 'Cfg.model.ReadingType',
            associationKey: 'readingTypes',
            name: 'readingTypes'
        }
    ]
});

