/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.service.AttributeTransformer', {
    singleton: true,

    transform: function (attributes, set) {
        var transformedAttributes = [],
            customPropertySetId = set.get('customPropertySetId'),
            attributeOrder = 1,
            getDefaultValue = function (attribute) {
                switch (attribute.propertyTypeInfo.simplePropertyType) {
                    case "BOOLEAN": {
                        return attribute.propertyValueInfo.defaultValue
                            ? Uni.I18n.translate('customattributesets.propertyValueInfo.defaultValue.true', 'CPS', 'True')
                            : Uni.I18n.translate('customattributesets.propertyValueInfo.defaultValue.false', 'CPS', 'False');
                    }
                        break;
                    case "QUANTITY": {
                        return attribute.propertyValueInfo.defaultValue && attribute.propertyValueInfo.defaultValue.id
                            ? attribute.propertyValueInfo.defaultValue.id.replace(/(-?\d*)\:-?\d*\:.*/, '$1') + ' ' + attribute.propertyValueInfo.defaultValue.displayValue
                            : '-';
                    }
                        break;
                    default : {
                        return attribute.propertyValueInfo.defaultValue;
                    }
                }
            };

        Ext.each(attributes, function (attribute) {
            var transformedAttribute = {};

            transformedAttribute.name = attribute.name;
            transformedAttribute.required = attribute.required;
            transformedAttribute.identifier = customPropertySetId + '.' +  attribute.key;
            transformedAttribute.customAttributeType = {};
            transformedAttribute.customAttributeType.name = attribute.propertyTypeInfo.typeSimpleName;
            transformedAttribute.customAttributeType.possibleValues = attribute.allValues;
            transformedAttribute.defaultValue = getDefaultValue(attribute);
            transformedAttribute.description = attribute.description;
            transformedAttribute.order = attributeOrder;
            attributeOrder += 1;
            transformedAttributes.push(transformedAttribute);
        });

        return transformedAttributes;
    }
});
