/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.CheckboxWithExplanation', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.checkbox-with-explanation',
    defaultType: 'checkboxfield',
    explanation: undefined,
    fieldLabel: undefined,
    name: undefined,
    defaultValue: false,

    initComponent: function() {
        var me = this;
        me.items = [
            {
                boxLabel: me.explanation,
                name: me.name,
                itemId: me.name + 'Checkbox',
                checked: me.defaultValue
            }
        ];
        me.callParent();
    }
});
