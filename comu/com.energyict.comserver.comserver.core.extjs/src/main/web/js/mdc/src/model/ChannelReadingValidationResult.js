Ext.define('Mdc.model.ChannelReadingValidationResult', {
    extend: 'Ext.data.Model',
    requires: [
        'Cfg.model.ValidationRule'
    ],
    fields: [
        {name: 'dataValidated', type: 'boolean'},
        {name: 'validationResult', type: 'string'},
        {name: 'valueType', type: 'string'}
    ],
    associations: [
        {
            name: 'validationRules',
            type: 'hasMany',
            model: 'Cfg.model.ValidationRule'
        }
    ]
});
