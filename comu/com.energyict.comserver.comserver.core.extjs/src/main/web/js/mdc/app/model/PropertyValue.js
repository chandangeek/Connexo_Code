Ext.define('Mdc.model.PropertyValue', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'value'},
        {name:'defaultValue', persist: false},
        {name:'inheritedValue', persist: false}
    ]
});