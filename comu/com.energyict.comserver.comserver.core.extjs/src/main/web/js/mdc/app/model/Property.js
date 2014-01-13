Ext.define('Mdc.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'key', type: 'string'},
        {name:'required', type: 'boolean'},
        {name:'isInheritedOrDefaultValue', type: 'boolean', defaultValue: true, persist: false}
    ],
    associations: [
        {name: 'propertyValueInfo', type: 'hasOne', model: 'Mdc.model.PropertyValue', associationKey: 'propertyValueInfo',
                   getterName: 'getPropertyValue', setterName: 'setPropertyValue', foreignKey: 'propertyValueInfo'},
        {name: 'propertyTypeInfo', type: 'hasOne', model: 'Mdc.model.PropertyType', associationKey: 'propertyTypeInfo',
                           getterName: 'getPropertyType', setterName: 'setPropertyType', foreignKey: 'propertyTypeInfo'}
    ],
    requires: [
           'Mdc.model.PropertyValue',
           'Mdc.model.PropertyType'
        ]
});