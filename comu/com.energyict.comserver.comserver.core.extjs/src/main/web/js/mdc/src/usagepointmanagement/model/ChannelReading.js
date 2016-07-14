Ext.define('Mdc.usagepointmanagement.model.ChannelReading', {
    extend: 'Uni.model.Version',
    fields: ['value', 'interval', 'readingTime', 'readingQualities', 'validationResult', 'dataValidated', 'validationAction', 'validationRules']
});