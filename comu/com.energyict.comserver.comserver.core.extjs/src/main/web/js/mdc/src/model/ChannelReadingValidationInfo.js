Ext.define('Mdc.model.ChannelReadingValidationInfo', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ChannelReadingValidationResult'
    ],
    fields: [
        {name: 'dataValidated', type: 'boolean'}
    ],
    associations: [
        {
            type: 'hasOne',
            associatedName: 'mainValidationInfo',
            associationKey: 'mainValidationInfo',
            model: 'Mdc.model.ChannelReadingValidationResult',
            getterName: 'getMainValidationInfo'
        },
        {
            type: 'hasOne',
            associatedName: 'bulkValidationInfo',
            associationKey: 'bulkValidationInfo',
            model: 'Mdc.model.ChannelReadingValidationResult',
            getterName: 'getBulkValidationInfo'
        }
    ]
});
