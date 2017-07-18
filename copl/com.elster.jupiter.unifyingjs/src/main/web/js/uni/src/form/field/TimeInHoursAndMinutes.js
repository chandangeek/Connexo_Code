/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.TimeInHoursAndMinutes', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.timeInHoursAndMinutes',
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
                valueToRaw: me.formatWithTwoDigits,
                margin: '0 5 5 0',
                listeners: {
                    blur: {
                        fn: me.numberFieldValidation,
                        scope: me
                    }
                }
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
                valueToRaw: me.formatWithTwoDigits,
                margin: '0 5 5 0',
                listeners: {
                    blur: {
                        fn: me.numberFieldValidation,
                        scope: me
                    }
                }
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
    },

    numberFieldValidation: function (field) {
        var me = this,
            value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    },

    formatWithTwoDigits: function (value) {
        var result = '00';
        if (value) {
            if (value < 10 && value > 0) {
                result = '0' + value;
            } else if (value >= 10) {
                result = value;
            }
        }
        return result;
    }
});
