/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.DecimalNumber', {
    extend: 'Uni.property.view.property.Number',
    getNormalCmp: function () {
        var me = this;
        var minValue = null;
        var maxValue = null;
        var allowDecimals = true;
        var rule = me.getProperty().getValidationRule();

        if (rule != null) {
            minValue = rule.get('minimumValue');
            maxValue = rule.get('maximumValue');
            allowDecimals = rule.get('allowDecimals');
        }

        return {
            xtype: 'numberfield',
            name: this.getName(),
            itemId: me.key + 'numberfield',
            width: me.width,
            hideTrigger: true,
            keyNavEnabled: false,
            mouseWheelEnabled: false,
            minValue: minValue,
            maxValue: maxValue,
            allowDecimals: allowDecimals,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            inputType: me.inputType,
            allowBlank: me.allowBlank,
            blankText: me.blankText,
            decimalPrecision: 20
        };
    }
});