/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Textarea', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;
        return {
            xtype: 'textareafield',
            name: this.getName(),
            itemId: me.key + 'textareafield',
            width: me.width,
            grow: true,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            inputType: me.inputType,
            allowBlank: me.allowBlank,
            blankText: me.blankText
        }
    },

    getField: function () {
        return this.down('textareafield');
    }
});