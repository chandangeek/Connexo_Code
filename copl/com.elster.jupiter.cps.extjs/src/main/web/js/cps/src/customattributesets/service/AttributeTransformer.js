Ext.define('Cps.customattributesets.service.AttributeTransformer', {
    singleton: true,

    transform: function (attributes) {
        var transformedAttributes = [],
            attributeOrder = 1;

        Ext.each(attributes, function(attribute) {
            var transformedAttribute = {};

            transformedAttribute.name = attribute.name;
            transformedAttribute.required = attribute.required;
            transformedAttribute.customAttributeType = {};
            transformedAttribute.customAttributeType.name = attribute.propertyTypeInfo.typeSimpleName;
            transformedAttribute.customAttributeType.possibleValues = attribute.allValues;
            if(attribute.propertyTypeInfo.simplePropertyType == "BOOLEAN"){
                transformedAttribute.defaultValue = attribute.propertyValueInfo.defaultValue ?
                    Uni.I18n.translate('customattributesets.propertyValueInfo.defaultValue.true', 'CPS', 'True') :
                    Uni.I18n.translate('customattributesets.propertyValueInfo.defaultValue.false', 'CPS', 'False');
            } else {
                transformedAttribute.defaultValue = attribute.propertyValueInfo.defaultValue;
            }
            transformedAttribute.description = attribute.description;
            transformedAttribute.order = attributeOrder;
            attributeOrder += 1;
            transformedAttributes.push(transformedAttribute);
        });

        return transformedAttributes;
    }
});
