Ext.define('Mdc.widget.TimeInfoField', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.timeInfoField',
    layout: 'hbox',
    combineErrors: true,
    msgTarget: 'side',
    submitFormat: 'c',

    valueCfg: null,
    unitCfg: null,

    initComponent: function () {
        var me = this;
        if (!me.valueCfg) {
            me.valueCfg = {};
        }
        if (!me.unitCfg) {
            me.unitCfg = {};
        }

        me.buildField();
        me.callParent();
        me.valueField = me.down('#valueField');
        me.unitField = me.down('#unitField');

        me.initField();
    },

    //@private
    buildField: function () {
        var me = this;
        me.items = [
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'valueField',
                hideTrigger: true,
                submitValue: false
            }, me.valueCfg),
            Ext.apply({
                xtype: 'textfield',
                itemId: 'unitField',
                submitValue: false,
                flex: 1
            }, me.unitCfg)]
    },

    getValue: function () {
        var me = this,
            value = parseInt(me.valueField.getSubmitValue()),
            timeUnit = me.unitField.getSubmitValue();

        if (!me.quantity) {
            return null;
        }

        return {
            count: value,
            timeUnit: timeUnit
        };
    },

    setValue: function (quantity) {
        var me = this;

        me.quantity = quantity;

        if (quantity) {
            me.valueField.setValue(quantity.count);
            me.unitField.setValue(quantity.timeUnit);
        }
    },

    getSubmitData: function () {
        var me = this,
            data = null;
        if (!me.disabled && me.submitValue && !me.isFileUpload()) {
            data = {};
            value = me.getValue();
            data[me.getName()] = '' + value ? value : null;
        }
        return data;
    }
});