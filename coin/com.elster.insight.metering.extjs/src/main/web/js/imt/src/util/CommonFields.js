Ext.define('Imt.util.CommonFields', {
    requires: [
        'Uni.form.field.ReadingTypeDisplay'
    ],
    singleton: true,

    prepareReadingTypeRequirementFields: function (readingTypeRequirements) {
        var fields = [],
            index = 0;

        readingTypeRequirements.each(function (rtr) {
            var field = {
                    itemId: 'reading-type-requirement-' + index++,
                    htmlEncode: false
                },
                pattern,
                patternTooltip;

            field.fieldLabel = Uni.I18n.translate('general.xMeterData', 'IMT', "'{0}' meter data", [rtr.get('meterRole').name]);

            switch (rtr.get('type')) {
                case 'fullySpecified':
                    field.xtype = 'reading-type-displayfield';
                    field.value = rtr.getReadingType().getData();
                    break;
                case 'partiallySpecified':
                    pattern = rtr.get('readingTypePattern');

                    patternTooltip = '<ul>';
                    Ext.iterate(pattern.attributes, function (key, value) {
                        switch (key) {
                            case 'multiplier':
                                if (Ext.Array.contains(value, '*')) {
                                    patternTooltip += '<li>' + Uni.I18n.translate('general.tooltip.anyMultiplier', 'IMT', 'Any multiplier') + '</li>';
                                }
                                break;
                            case 'timePeriod':
                                if (Ext.Array.contains(value, '*')) {
                                    patternTooltip += '<li>' + Uni.I18n.translate('general.tooltip.anyTimePeriod', 'IMT', 'Any time period') + '</li>';
                                }
                                break;
                            case 'accumulation':
                                if (Ext.Array.contains(value, '*')) {
                                    patternTooltip += '<li>' + Uni.I18n.translate('general.tooltip.anyAccumulation', 'IMT', 'Any accumulation') + '</li>';
                                }
                                break;
                            case 'unit':
                                if (Ext.isArray(value) && value.length) {
                                    patternTooltip += '<li>' + Uni.I18n.translate('general.tooltip.unitsOfMeasure', 'IMT', '{0} units of measure', [value.join(', ')]) + '</li>';
                                }
                                break;
                        }
                    });
                    patternTooltip += '</ul>';

                    field.value = pattern.value;
                    field.value += '<span class="uni-icon-info-small" style="display: inline-block; width: 16px; height: 16px; margin-left: 10px;" data-qtip="'
                        + Uni.I18n.translate('general.tooltip.readingTypePattern', 'IMT', 'This reading type pattern matches a reading types with:{0}', [patternTooltip], false)
                        + '"></span>';
                    break;
            }

            fields.push(field);
        });

        return fields;
    },

    prepareCustomProperties: function(properties, upCustomPropertySets) {
        var attributes = '',
            valueRegExp = /(-?\d*)\:-?\d*\:.*/,
            propertyValue;
        properties.each(function (cps) {
            attributes += cps.get('name');

            if(upCustomPropertySets){
                upCustomPropertySets.each(function (upCustomPropertySet) {
                    if(upCustomPropertySet.get('customPropertySetId') == cps.get('customPropertySet').id){
                        upCustomPropertySet.properties().each(function(property){
                            if(cps.get('key') == property.get('key')){
                                if(property.getPropertyValue().get('value') && property.getPropertyValue().get('value').displayValue){
                                    propertyValue = property.getPropertyValue().get('value').id.replace(valueRegExp, '$1') + ' '
                                        + property.getPropertyValue().get('value').displayValue;
                                } else if (property.getPropertyValue().get('value')){
                                    propertyValue = property.getPropertyValue().get('value');
                                } else {
                                    propertyValue = '-'
                                }
                                attributes += ': ' + propertyValue;
                            }
                        });

                    }
                });

            }
            attributes += '<span class="icon-info" style="display: inline-block; font-size:16px; margin-left: 16px" data-qtip="'
                + Uni.I18n.translate('general.tooltip.partOfCustomAttributeSet', 'IMT', 'Part of {0} custom attribute set', [cps.get('customPropertySet').name])
                + '"></span>';
            attributes += '<br>';
        });

        return attributes
            ? {
            itemId: 'purpose-formula-attributes',
            fieldLabel: Uni.I18n.translate('general.attributes', 'IMT', 'Attributes'),
            htmlEncode: false,
            value: attributes
        } : null;
    }
});