/**
 * @class Uni.property.model.Property
 */
Ext.define('Uni.property.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'key', type: 'string'},
        {name: 'required', type: 'boolean'},
        {name: 'value', persist: false},
        {name: 'default', persist: false},
        {name: 'isInheritedOrDefaultValue', type: 'boolean', defaultValue: true, persist: false}
    ],
    requires: [
        'Uni.property.model.PropertyValue',
        'Uni.property.model.PropertyType'
    ],
    associations: [
        {
            name: 'propertyValueInfo',
            type: 'hasOne',
            model: 'Uni.property.model.PropertyValue',
            associationKey: 'propertyValueInfo',
            getterName: 'getPropertyValue',
            setterName: 'setPropertyValue',
            foreignKey: 'propertyValueInfo'
        },
        {
            name: 'propertyTypeInfo',
            type: 'hasOne',
            model: 'Uni.property.model.PropertyType',
            associationKey: 'propertyTypeInfo',
            getterName: 'getPropertyType',
            setterName: 'setPropertyType',
            foreignKey: 'propertyTypeInfo'
        }
    ],

    /**
     * Sets property values and defaults based on property associated objects
     */
    initValues: function () {
        var me = this;
        var value = null;
        var restoreValue = '';
        var isInheritedValue = true;

        // was on try-catch
        if (me.raw['propertyValueInfo']) {
            var propertyValue = me.getPropertyValue() || null;

            if (null !== propertyValue) {
                value = propertyValue.get('value');
                isInheritedValue = false;

                if (propertyValue.get('inheritedValue') !== '') {
                    restoreValue = propertyValue.get('inheritedValue');
                } else {
                    restoreValue = propertyValue.get('defaultValue');
                }

                if (value === '') {
                    value = restoreValue;
                    isInheritedValue = true;
                }
            }
        }

        me.set('isInheritedOrDefaultValue', isInheritedValue);
        me.set('value', value);
        me.set('default', restoreValue);
    },

    getType: function () {
        return this.getPropertyType().get('simplePropertyType');
    },

    getValidationRule: function () {
        var propertyType = this.getPropertyType();

        if (propertyType.raw['propertyValidationRule']) {
            return propertyType.getPropertyValidationRule();
        } else {
            return null;
        }
    },

    getPredefinedPropertyValues: function () {
        var propertyType = this.getPropertyType();

        if (propertyType.raw['predefinedPropertyValuesInfo']) {
            return propertyType.getPredefinedPropertyValue();
        } else {
            return null;
        }
    },

    getPossibleValues: function () {
        var values = this.getPredefinedPropertyValues();
        return values
            ? values.get('possibleValues')
            : null
            ;
    },

    getSelectionMode: function () {
        var values = this.getPredefinedPropertyValues();
        return values
            ? values.get('selectionMode')
            : null
            ;
    },

    getExhaustive: function () {
        var values = this.getPredefinedPropertyValues();
        return values
            ? values.get('exhaustive')
            : null
            ;
    }
});