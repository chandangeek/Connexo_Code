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
            transformedAttribute.customAttributeType.name = attribute.typeSimpleName;
            transformedAttribute.customAttributeType.possibleValues = attribute.allValues;
            transformedAttribute.defaultValue = attribute.defaultValue;
            transformedAttribute.description = attribute.description;
            transformedAttribute.order = attributeOrder;
            attributeOrder += 1;
            transformedAttributes.push(transformedAttribute);
        });

        return transformedAttributes;
    }
});
