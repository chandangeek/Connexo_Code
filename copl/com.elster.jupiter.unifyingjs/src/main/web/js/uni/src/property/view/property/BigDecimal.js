/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.BigDecimal', {
    extend: 'Uni.property.view.property.BaseCombo',


    getNormalCmp: function () {
        var me = this;
        return {
                xtype: 'textfield',
                name: this.getName(),
                itemId: me.key + 'bigdecimalfield',
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
                maskRe: me.initAllowDecimals() ? /[0-9.,-]/ : /[0-9-]/
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

    getField: function () {
        return this.down('textfield') ? this.down('textfield') : this.down('displayfield');
    },

    markInvalid: function (error) {
        var cmp;
        cmp = this.getField();
        cmp.markInvalid(error);
    },

    clearInvalid: function () {
        this.getField().clearInvalid();
    },
    initMinValue: function(){
        var me = this;
        var rule = me.getProperty().getValidationRule();
        if (rule) {
            return rule.get('minimumValue');
        }
        var predefinedPropertyValuesInfo =  me.getProperty().getPropertyType().predefinedPropertyValuesInfo;
        var min = predefinedPropertyValuesInfo ? predefinedPropertyValuesInfo.possibleValues[0] : null;
        return min ;
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
        return max;
    },
    initAllowDecimals: function(){
        var me = this;
        return true;
        var rule = me.getProperty().getValidationRule();
        if (rule) {
            return rule.get('allowDecimals');
        }
        return false;
    },
    getSortFunctionForPossibleValues: function() {
        return function(a, b) {
            function splitDecimalNumber(number){
                return (number.indexOf(",") !== -1) ? number.split(',') : number.split('.');
            }
            function comparePositiveNumbers(splittedFirstNumber, splittedSecondNumber){
                if (splittedFirstNumber[0].length !== splittedSecondNumber[0].length){
                    return (splittedFirstNumber[0].length - splittedSecondNumber[0].length);
                }else{
                    if (splittedFirstNumber[0] !== splittedSecondNumber[0]){
                        return (splittedFirstNumber[0] > splittedSecondNumber[0] ? 1 : -1)
                    }else{
                        if (!splittedFirstNumber[1]) splittedFirstNumber[1] = "";
                        if (!splittedSecondNumber[1]) splittedSecondNumber[1] = "";
                        return splittedFirstNumber[1].localeCompare(splittedSecondNumber[1])

                    }
                }
            }
            if (a[0] !== "-" && b[0] !== "-"){
                return comparePositiveNumbers(splitDecimalNumber(a), splitDecimalNumber(b));
            }else if (a[0] !== "-" || b[0] !== "-"){
                return (a[0] !== "-") ? 1 : -1
            }else{
                return comparePositiveNumbers(splitDecimalNumber(b), splitDecimalNumber(a));
            }

        }
    }
});