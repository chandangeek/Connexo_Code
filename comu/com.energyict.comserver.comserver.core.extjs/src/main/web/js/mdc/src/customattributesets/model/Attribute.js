Ext.define('Mdc.customattributesets.model.Attribute', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'name', type: 'string'},
        {name: 'required', type: 'boolean'},
        {name: 'customAttributeType', type: 'auto'},
        {name: 'defaultValue', type: 'string'}
    ]
});
