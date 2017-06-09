/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Number', {
    extend: 'Uni.property.view.property.BaseCombo',
    width: 128,

    getNormalCmp: function () {
        var me = this;
        return {
            xtype: 'numberfield',
            name: this.getName(),
            itemId: me.key + 'numberfield',
            width: me.width,
            hideTrigger: false,
            keyNavEnabled: false,
            mouseWheelEnabled: false,
            minValue: me.initMinValue(),
            maxValue: me.initMaxValue(),
            allowDecimals: me.initAllowDecimals(),
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
        return this.callParent(arguments);
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
    },
    initMinValue: function(){
        var me = this;
        var rule = me.getProperty().getValidationRule();
        if (rule) {
            return rule.get('minimumValue');
        }
        var predefinedPropertyValuesInfo =  me.getProperty().getPropertyType().predefinedPropertyValuesInfo;
        var min = predefinedPropertyValuesInfo ? predefinedPropertyValuesInfo.possibleValues[0] : null;
        // quick fix. Extjs numberfield works incorrect with numbers which are not in range -9000000000000000 - 9000000000000000.
        return  (min || min === 0) ? min : -9000000000000000 ;
    },
    initMaxValue: function(){
        var me = this;
        var rule = me.getProperty().getValidationRule();
        if (rule) {
            return rule.get('maximumValue');
        }
        var predefinedPropertyValuesInfo =  me.getProperty().getPropertyType().predefinedPropertyValuesInfo;
        var max = null;
        if(predefinedPropertyValuesInfo) {
            var range = predefinedPropertyValuesInfo.possibleValues.length - 1;
            max = predefinedPropertyValuesInfo.possibleValues[range]
        }
        // quick fix. Extjs numberfield works incorrect with numbers which are not in range -9000000000000000 - 9000000000000000.
        return (max || max === 0) ? max : 9000000000000000;
    },
    initAllowDecimals: function(){
        var me = this;
        var rule = me.getProperty().getValidationRule();
        if (rule) {
            return rule.get('allowDecimals');
        }
        return false;
    },

    getSortFunctionForPossibleValues: function() {
        return function(a, b) { return a - b; };
    }

});