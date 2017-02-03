/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Text', {
    extend: 'Uni.property.view.property.BaseCombo',


    getNormalCmp: function () {
        var me = this,
            maxLength = me.property.raw.propertyTypeInfo.propertyValidationRule ? me.property.raw.propertyTypeInfo.propertyValidationRule.maxLength : undefined;
        return {
            xtype: 'textfield',
            name: this.getName(),
            itemId: me.key,
            width: me.width,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            allowBlank: me.allowBlank,
            inputType: me.inputType,
            maxLength: maxLength,
            enforceMaxLength: (maxLength != null) ? true : false,
            blankText: me.blankText
        }
    },

    getField: function () {
        return this.down('textfield');
    },

    markInvalid: function (error) {
        var cmp;
        cmp = this.isCombo() ? this.getComboField() : this.getField();
        cmp.markInvalid(error);
    },

    clearInvalid: function () {
        this.getField().clearInvalid();
    }
});