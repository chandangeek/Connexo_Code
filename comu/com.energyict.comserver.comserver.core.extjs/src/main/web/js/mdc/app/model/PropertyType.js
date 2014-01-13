Ext.define('Mdc.model.PropertyType', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'simplePropertyType'}
    ],
    associations: [
        {name: 'predefinedPropertyValuesInfo', type: 'hasOne', model: 'Mdc.model.PredefinedPropertyValue', associationKey: 'predefinedPropertyValuesInfo',
            getterName: 'getPredefinedPropertyValue', setterName: 'setPredefinedPropertyValue', foreignKey: 'predefinedPropertyValuesInfo'},
        {name: 'propertyValidationRule', type: 'hasOne', model: 'Mdc.model.PropertyValidationRule', associationKey: 'propertyValidationRule',
                    getterName: 'getPropertyValidationRule', setterName: 'setPropertyValidationRule', foreignKey: 'propertyValidationRule'}
    ],
    requires: [
        'Mdc.model.PredefinedPropertyValue',
        'Mdc.model.PropertyValidationRule'
    ]
});