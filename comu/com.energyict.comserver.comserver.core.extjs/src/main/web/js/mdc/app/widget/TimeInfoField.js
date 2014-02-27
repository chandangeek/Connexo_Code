Ext.define('Mdc.widget.TimeInfoField', {
    extend: 'Ext.form.FieldContainer',
    requires: [
        'Mdc.store.TimeUnits'
    ],
    stores: [
        'TimeUnits'
    ],
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.timeInfoField',
    layout: 'hbox',
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
        var timeUnits = Ext.create('Mdc.store.TimeUnits');
        var me = this;
        me.items = [
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'valueField',
                hideTrigger: true,
                submitValue: false
            }, me.valueCfg),
            Ext.apply({
                xtype: 'combobox',
                itemId : 'unitField',
                store: timeUnits,
                queryMode: 'local',
                displayField: 'timeUnit',
                valueField: 'timeUnit',
                submitValue: false
            }, me.unitCfg)]
    },

    getValue: function () {
        var me = this,
            value = parseInt(me.valueField.getSubmitValue()),
            timeUnit = me.unitField.getSubmitValue();

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
    },

    markInvalid: function(fields){
        debugger;
        this.eachItem(function(field){
            field.markInvalid(fields);
        });
    },

    eachItem: function(fn, scope) {
        if(this.items && this.items.each){
            this.items.each(fn, scope || this);
        }
    }
});