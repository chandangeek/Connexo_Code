Ext.define('Mdc.model.PropertyValidationRule', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'allowDecimals'},
        {name:'minimumValue'},
        {name:'maximumValue'}
    ]
});