/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.AtPeriod
 */
Ext.define('Uni.form.field.AtPeriod', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-form-field-atperiod',

    fieldLabel: Uni.I18n.translate('general.at', 'UNI', 'At'),

    layout: {
        type: 'hbox'
    },

    lastHourTask: undefined,
    lastMinuteTask: undefined,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);

        me.on('afterrender', me.initListeners, me);
    },

    buildItems: function () {
        var me = this;

        me.items = [
            {
                xtype: 'numberfield',
                itemId: 'hour-field',
                hideLabel: true,
                valueToRaw: me.formatDisplayOfTime,
                value: 0,
                minValue: 0,
                maxValue: 23,
                allowBlank: false,
                width: 64,
                listeners: {
                    blur: {
                        fn: me.numberFieldValidation,
                        scope: me
                    }
                }
            },
            {
                xtype: 'label',
                itemId: 'separator-field',
                text: ':',
                margin: '6 6 0 6',
                cls: Ext.baseCSSPrefix + 'form-item-label',
                style: {
                    fontWeight: 'normal'
                }
            },
            {
                xtype: 'numberfield',
                itemId: 'minute-field',
                hideLabel: true,
                valueToRaw: me.formatDisplayOfTime,
                value: 0,
                minValue: 0,
                maxValue: 59,
                allowBlank: false,
                width: 64,
                margin: '0 6 0 0',
                listeners: {
                    blur: {
                        fn: me.numberFieldValidation,
                        scope: me
                    }
                }
            },
            {
                xtype: 'label',
                itemId: 'minutes-unit-field',
                text: Uni.I18n.translate('period.minutes', 'UNI', 'minute(s)'),
                cls: Ext.baseCSSPrefix + 'form-item-label',
                style: {
                    fontWeight: 'normal'
                },
                hidden: true
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getHourField().on('change', function () {
            if (me.lastHourTask) {
                me.lastHourTask.cancel();
            }

            me.lastHourTask = new Ext.util.DelayedTask(function () {
                var newValue = me.getValue();
                if (Ext.isEmpty(newValue)) return;
                me.fireEvent('periodchange', newValue);
            });

            me.lastHourTask.delay(256);
        }, me);

        me.getMinuteField().on('change', function () {
            if (me.lastMinuteTask) {
                me.lastMinuteTask.cancel();
            }

            me.lastMinuteTask = new Ext.util.DelayedTask(function () {
                var newValue = me.getValue();
                if (Ext.isEmpty(newValue)) return;
                me.fireEvent('periodchange', newValue);
            });

            me.lastMinuteTask.delay(256);
        }, me);
    },

    getHourField: function () {
        return this.down('#hour-field');
    },

    getMinuteField: function () {
        return this.down('#minute-field');
    },

    getValue: function () {
        var me = this,
            hourField = me.getHourField(),
            minuteField = me.getMinuteField(),
            hourValue = hourField.getValue(),
            minuteValue = minuteField.getValue();

        if (hourValue < hourField.minValue || hourValue > hourField.maxValue || minuteValue < minuteField.minValue || minuteValue > minuteField.maxValue) {
            return undefined;
        }

        return {
            atHour: hourValue,
            atMinute: minuteValue
        };
    },

    // TODO Use the date-time xtype for this.
    formatDisplayOfTime: function (value) {
        var result = '00';

        if (value) {
            if (value < 10 && value > 0) {
                result = '0' + value;
            } else if (value >= 10) {
                result = value;
            }
        }
        return result;
    },

    numberFieldValidation: function (field) {
        var me = this,
            value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    }

});