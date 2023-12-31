/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.model.PropertyType', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'simplePropertyType'}
    ],
    requires: [
        'Uni.property.model.PredefinedPropertyValue',
        'Uni.property.model.PropertyValidationRule',
        'Uni.property.model.PropertyParams'
    ],
    associations: [
        {
            name: 'predefinedPropertyValuesInfo',
            type: 'hasOne',
            model: 'Uni.property.model.PredefinedPropertyValue',
            associationKey: 'predefinedPropertyValuesInfo',
            getterName: 'getPredefinedPropertyValue',
            setterName: 'setPredefinedPropertyValue',
            foreignKey: 'predefinedPropertyValuesInfo'
        },
        {
            name: 'propertyValidationRule',
            type: 'hasOne',
            model: 'Uni.property.model.PropertyValidationRule',
            associationKey: 'propertyValidationRule',
            getterName: 'getPropertyValidationRule',
            setterName: 'setPropertyValidationRule',
            foreignKey: 'propertyValidationRule'
        },
        {
            name: 'params',
            type: 'hasOne',
            model: 'Uni.property.model.PropertyParams',
            associationKey: 'params',
            getterName: 'getPropertyParams',
            setterName: 'setPropertyParams',
            foreignKey: 'params'
        }
    ]
});