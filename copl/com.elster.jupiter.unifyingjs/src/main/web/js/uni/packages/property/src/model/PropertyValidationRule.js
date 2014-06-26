Ext.define('Uni.property.model.PropertyValidationRule', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'allowDecimals'},
        {name: 'minimumValue'},
        {name: 'maximumValue'}
    ]
});