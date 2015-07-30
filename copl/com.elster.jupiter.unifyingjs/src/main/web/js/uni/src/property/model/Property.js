/**
 * @class Uni.property.model.Property
 */
Ext.define('Uni.property.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'key', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'required', type: 'boolean'},
        {name: 'value', persist: false},
        {name: 'default', persist: false},
        {name: 'hasDefault', persist: false},
        {name: 'hasValue', persist:false},
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

    isEdited: false,

    /**
     * Sets property values and defaults based on property associated objects
     */
    initValues: function () {
        var me = this;
        var value = null;
        var restoreValue = '';
        var isInheritedValue = true;
        var hasDefaultValue = false;
        var hasValue = false;

        // was on try-catch
        if (me.raw['propertyValueInfo']) {
            var propertyValue = me.getPropertyValue() || null;

            if (null !== propertyValue) {
                value = propertyValue.get('value');
                isInheritedValue = false;
                if (!propertyValue.get('propertyHasValue')) {
                    if (_.isEqual(value, propertyValue.get('defaultValue'))) {
                        isInheritedValue = true;
                    }

                    if (propertyValue.get('inheritedValue') !== '') {
                        restoreValue = propertyValue.get('inheritedValue');
                    } else {
                        restoreValue = propertyValue.get('defaultValue');
                        if (typeof propertyValue.get('defaultValue') !== 'undefined' && typeof propertyValue.get('defaultValue') !== '') {
                            hasDefaultValue = true;
                        }
                    }

                    if (value === '') {
                        value = restoreValue;
                        isInheritedValue = true;
                    }
                }
                hasValue = propertyValue.get('propertyHasValue');
            }
        }
        me.beginEdit();
        me.set('isInheritedOrDefaultValue', isInheritedValue);
        me.set('value', value);
        me.set('default', restoreValue);
        me.set('hasDefaultValue', hasDefaultValue);
        me.set('hasValue', hasValue);
        me.endEdit();
    },

    initInheritedValues: function() {
        var me = this;
        var value = null;
        var hasDefaultValue = false;
        var isDefaultValue = false;
        var hasValue = false;

        // was on try-catch
        if (me.raw['propertyValueInfo']) {
            var propertyValue = me.getPropertyValue() || null;
            if (null !== propertyValue) {
                value = propertyValue.get('value');
                if (!propertyValue.get('propertyHasValue')) {
                    if (_.isEqual(value, propertyValue.get('defaultValue'))) {
                        isDefaultValue = true;
                    }

                    if (!value) {
                        value = propertyValue.get('defaultValue');
                        hasDefaultValue = true;
                    }
                }
                propertyValue.set('inheritedValue', value);
                propertyValue.set('value', '');
                hasValue = propertyValue.get('propertyHasValue');
            }

        }
        if (isDefaultValue || (typeof me.raw['propertyValueInfo'] === 'undefined')) {
            me.set('isInheritedOrDefaultValue', true);
        } else {
            me.set('isInheritedOrDefaultValue', false);
        }
        me.set('value', value);
        me.set('default', value);
        me.set('hasDefaultValue', hasDefaultValue);
        me.set('hasValue', hasValue);
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