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
    numberFieldWidth: 100,
    unitFieldWidth: 185,
    flex: 1,

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
                hideTrigger: false,
                submitValue: false,
                width: me.numberFieldWidth,
                margin: '0 5 5 0'
            }, me.valueCfg),
            Ext.apply({
                xtype: 'combobox',
                itemId : 'unitField',
                store: timeUnits,
                queryMode: 'local',
                displayField: 'timeUnit',
                valueField: 'timeUnit',
                submitValue: false,
                forceSelection: true,
                width: me.unitFieldWidth
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
        this.eachItem(function(field){
            field.markInvalid('');
        });
        this.items.items[0].markInvalid(fields);
    },

    eachItem: function(fn, scope) {
        if(this.items && this.items.each){
            this.items.each(fn, scope || this);
        }
    }
});