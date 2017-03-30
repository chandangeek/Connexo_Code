/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.ComboReturnedRecordData', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.combo-returned-record-data',

    getValue: function () {
        var me = this,
            value = me.callParent(arguments),
            record;

        if (!Ext.isEmpty(value)) {
            record = me.findRecordByValue(value);
            value = record ? record.getData() : null;
        }

        return value;
    },

    setValue: function () {
        var me = this;

        if (Ext.isObject(arguments[0])) {
            arguments[0] = arguments[0][me.valueField];
        }

        me.callParent(arguments);
    }
});