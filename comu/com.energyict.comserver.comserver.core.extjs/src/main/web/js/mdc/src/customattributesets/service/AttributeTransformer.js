Ext.define('Mdc.customattributesets.service.AttributeTransformer', {
    singleton: true,

    transform: function (attributes) {
        var transformedAttributes = [];

        Ext.each(attributes, function(attribute) {
            var transformedAttribute = {},
                valueFactory = attribute.valueFactory,
                possibleValues = attribute.possibleValues;

            transformedAttribute.name = attribute.name;
            transformedAttribute.required = attribute.required;
            if (valueFactory) {
                transformedAttribute.customAttributeType = {};
                transformedAttribute.customAttributeType.name = valueFactory.valueType;
                if (possibleValues && !Ext.isEmpty(possibleValues.allValues)) transformedAttribute.customAttributeType.possibleValues = possibleValues.allValues;
            }
            if (possibleValues) transformedAttribute.defaultValue = possibleValues.default;
            transformedAttributes.push(transformedAttribute);
        });

        return transformedAttributes;
    }
});
