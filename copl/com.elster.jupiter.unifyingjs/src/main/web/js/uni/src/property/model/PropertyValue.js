Ext.define('Uni.property.model.PropertyValue', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'value'},
        {name: 'defaultValue'},
        {name: 'inheritedValue'},
        {name: 'propertyHasValue', type:'boolean', persist: false}
    ]
});