/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.widget.TimeInSecondsField', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.timeInSecondsField',
    layout: 'hbox',
    msgTarget: 'under',
    submitFormat: 'c',

    initComponent: function () {
        var me = this;

        me.buildField();
        me.callParent();
        me.hourField = me.down('#hourField');
        me.minuteField = me.down('#minuteField');

        me.initField();
    },

    //@private
    buildField: function () {
        var me = this;
        me.items = [
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'hourField',
                hideTrigger: false,
                submitValue: false,
                width: 70,
                minValue: 0,
                maxValue: 23,
                margin: '0 5 5 0'
            }, me.valueCfg),
            Ext.apply({
                xtype: 'displayfield',
                value: ':',
                margin: '0 5 5 0'
            }),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'minuteField',
                hideTrigger: false,
                submitValue: false,
                minValue: 0,
                maxValue: 59,
                width: 70,
                margin: '0 5 5 0'
            })]
    },

    getValue: function () {
        var me = this,
            hours = parseInt(me.hourField.getSubmitValue()),
            minutes = parseInt(me.minuteField.getSubmitValue());

        return hours * 3600 + minutes * 60;
    },

    setValue: function (value) {
        var me = this;
        me.hourField.setValue(Math.floor(value/3600));
        me.minuteField.setValue((value%3600)/60);
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

    markInvalid: function (fields) {
        this.eachItem(function (field) {
            field.markInvalid('');
        });
        this.items.items[0].markInvalid(fields);
    },

    eachItem: function (fn, scope) {
        if (this.items && this.items.each) {
            this.items.each(fn, scope || this);
        }
    }
});
