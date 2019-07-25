/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.MailTo', {
    extend: 'Uni.property.view.property.BaseCombo',


    getNormalCmp: function () {
        var me = this,
            maxLength = me.property.raw.propertyTypeInfo.propertyValidationRule ? me.property.raw.propertyTypeInfo.propertyValidationRule.maxLength : undefined;
        return {
            xtype: 'fieldcontainer',
            visible: true,
            layout: 'hbox',
            required: true,
            items: [{
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
                },
                {
                    xtype: 'button',
                    itemId: 'txt-user-name-info',
                    tooltip: Uni.I18n.translate('dataExport.recipients.tooltip', 'DES', 'Separate multiple e-mailaddresses by semicolons (;)'),
                    text: '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                    disabled: true, // to avoid a hand cursor
                    ui: 'blank',
                    shadow: false,
                    margin: '6 0 0 10',
                    width: 16,
                    tabIndex: -1
                }]
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