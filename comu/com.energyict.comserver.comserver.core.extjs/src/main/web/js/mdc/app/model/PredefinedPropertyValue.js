Ext.define('Mdc.model.PredefinedPropertyValue', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'exhaustive', type: 'boolean'},
        {name: 'selectionMode', type: 'string'},
        {name: 'possibleValues', type:'auto'}
    ]
});