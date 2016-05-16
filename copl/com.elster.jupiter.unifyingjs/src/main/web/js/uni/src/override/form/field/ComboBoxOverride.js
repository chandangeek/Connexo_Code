/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Uni.override.form.field.ComboBoxOverride', {
    override: 'Ext.form.field.ComboBox',

    anyMatch: true,
    valueIsRecordData: false,

    listeners: {
        // force re-validate on combo change
        change: function (combo) {
            combo.validate();
        }
    },

    initComponent: function () {
        var me=this;
        me.listConfig = me.listConfig || {};
        Ext.apply(me.listConfig, {
            getInnerTpl: function (displayField) {
                return '{' + displayField  + ':htmlEncode}';
            }
        });
        this.callParent(arguments);
    },

    getValue: function () {
        var me = this,
            value = me.callParent(arguments),
            record;

        if (me.valueIsRecordData && !Ext.isEmpty(value)) {
            record = me.findRecordByValue(value);
            value = record ? record.getData() : null;
        }

        return value;
    }
});