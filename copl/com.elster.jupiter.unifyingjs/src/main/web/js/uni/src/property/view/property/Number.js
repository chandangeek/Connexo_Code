/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Number', {
    extend: 'Uni.property.view.property.BaseCombo',
    width: 128,

    getNormalCmp: function () {
        var me = this;
        var min = me.property.raw.propertyTypeInfo.predefinedPropertyValuesInfo ? me.property.raw.propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[0] : null;
        var minValue = (min || min === 0) ? min : -9000000000000000 ;
        var max = null;
        if(me.property.raw.propertyTypeInfo.predefinedPropertyValuesInfo) {
            var range = me.property.raw.propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues;
            max = range[range.length-1]
        }

        var maxValue = (max || max === 0) ? max : 9000000000000000;
        return {
            xtype: 'numberfield',
            name: this.getName(),
            itemId: me.key + 'numberfield',
            width: me.width,
            hideTrigger: false,
            keyNavEnabled: false,
            mouseWheelEnabled: false,
            minValue: minValue,
            maxValue: maxValue,
            allowDecimals: false,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            inputType: me.inputType,
            allowBlank: me.allowBlank,
            blankText: me.blankText,
            listeners: {
                change: {
                    fn: me.checkValidNumber,
                    scope: me
                }
            }
        };
    },

    checkValidNumber: function () {
        var me = this,
            field = me.getField(),
            number = field.getValue();

        if (Ext.isNumber(number)) {
            if (number > field.maxValue) me.getField().setValue(field.maxValue);
            if (number < field.minValue) me.getField().setValue(field.minValue);
        } else {
            me.getField().setValue(null);
        }
    },

    getDisplayCmp: function () {
        return {
            xtype: 'displayfield',
            name: this.getName(),
            itemId: this.key + 'displayfield',
            renderer: function(value) {
                return Ext.isEmpty(value) ? '-' : value;
            }
        }
    },

    getComboCmp: function () {
        var result = this.callParent(arguments);
        result.fieldStyle = 'text-align:right;';

        return result;
    },

    markInvalid: function (error) {
        this.getField().markInvalid(error);
    },

    clearInvalid: function () {
        this.getField().clearInvalid();
    },

    getField: function () {
        var me = this;
        if (me.isCombo()) {
            return me.down('combobox')
        } else {
            return me.down('numberfield')
        }
    }
});