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
    }
});