Ext.define('Mdc.model.ChannelReadingValidationResult', {
    extend: 'Ext.data.Model',
    requires: [
        'Cfg.model.ValidationRule'
    ],
    fields: [
        {name: 'validationResult', type: 'string'},
        'validationRules',
        'valueModificationFlag',
        'estimatedByRule'
    ]
});
