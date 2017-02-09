/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.form.field.Base', {
    override: "Ext.form.field.Base",
    labelAlign: 'right',
    labelPad: 15,
    msgTarget: 'under',
    blankText: 'This is a required field',
    validateOnChange: false,
    validateOnBlur: false,

    getLabelCls: function () {
        var labelCls = this.labelCls;
        if (this.required) {
            labelCls += ' ' + 'uni-form-item-label-required';
        }

        return labelCls;
    },

    initComponent: function() {
        this.callParent(arguments);
    }
});


