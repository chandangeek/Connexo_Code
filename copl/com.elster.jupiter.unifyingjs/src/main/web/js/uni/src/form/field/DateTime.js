/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.DateTime
 *
 * This class contains the DateTime field.
 *
 *     Ext.create('Uni.form.field.DateTime', {
 *       itemId: 'endOfInterval',
 *       name: 'intervalStart',
 *       fieldLabel: Uni.I18n.translate('general.from', 'UNI', 'From'),
 *       labelAlign: 'top',
 *       dateConfig: {
 *         width: 100,
 *         submitValue: true,
 *       }
 *       hoursConfig: {
 *         maxValue: 20
 *       }
 *       minutesConfig: {
 *         minValue: 0
 *       }
 *       separatorConfig: {
 *         html: ':',
 *       }
 *     });
 *
 */
Ext.define('Uni.form.field.DateTime', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.date-time',
    layout: 'vbox',
    requires: [
        'Ext.form.field.Date',
        'Ext.form.field.Number',
        'Ext.container.Container'
    ],

    /**
     * @cfg {Object} dateConfig
     * Configuration for dateField allows you override or add any property of this field.
     */
    dateConfig: null,

    /**
     * @cfg {Object} hoursConfig
     * Configuration for hoursField allows you override or add any property of this field.
     */
    hoursConfig: null,

    /**
     * @cfg {Object} separatorConfig
     * Configuration for separatorField allows you override or add any property of this field.
     */
    separatorConfig: null,

    /**
     * @cfg {Object} minutesConfig
     * Configuration for minutesField allows you override or add any property of this field.
     */
    minutesConfig: null,

    secondsConfig: null,
    minutesSecondsConfig:null,
    dateTimeSeparatorConfig: null,
    valueInMilliseconds: false,

    initComponent: function () {
        var me = this,
            dateField = {
                xtype: 'datefield',
                itemId: 'date-time-field-date',
                submitValue: false,
                allowBlank: false,
                format: 'd M \'y',
                width: '100%',
                editable: false,
                beforeBlur: Ext.emptyFn,
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    },
                    blur: {
                        fn: me.fireBlurEvent,
                        scope: me
                    }
                }
            },
            hoursField = {
                itemId: 'date-time-field-hours',
                flex: 1,
                maxValue: 23,
                minValue: 0
            },
            minutesField = {
                itemId: 'date-time-field-minutes',
                flex: 1,
                maxValue: 59,
                minValue: 0
            },
            secondsField = {
                itemId: 'date-time-field-seconds',
                flex:1,
                hidden:true,
                maxValue:59,
                minValue: 0
            },
            separator = {
                xtype: 'component',
                itemId: 'hours-minutes-separator',
                html: ':',
                margin: '0 5 0 5'
            },
            secondsSeparator = {
                xtype: 'component',
                itemId: 'minutes-seconds-separator',
                hidden:true,
                html: ':',
                margin: '0 5 0 5'
            },
            dateTimeSeparator = {
                xtype: 'component',
                itemId: 'date-time-separator',
                html: '',
                margin: '0 5 0 5'
            },
            container = {
                xtype: 'container',
                width: '100%',
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                defaults: {
                    xtype: 'numberfield',
                    allowDecimals: false,
                    submitValue: false,
                    allowExponential: false,
                    value: 0,
                    valueToRaw: me.formatDisplayOfTime,
                    listeners: {
                        change: {
                            fn: me.onItemChange,
                            scope: me
                        },
                        blur: {
                            fn: me.numberFieldValidation,
                            scope: me
                        }
                    }
                }
            };

        if (me.layout === 'hbox') {
            delete container.width;
            dateField.width = 130;
            hoursField.width = 80;
            minutesField.width = 80;
            secondsField.width = 80;
        }

        Ext.apply(dateField, me.dateConfig);
        Ext.apply(hoursField, me.hoursConfig);
        Ext.apply(minutesField, me.minutesConfig);
        Ext.apply(secondsField, me.secondsConfig);
        Ext.apply(separator, me.separatorConfig);
        Ext.apply(secondsSeparator, me.minutesSecondsConfig);
        Ext.apply(dateTimeSeparator, me.dateTimeSeparatorConfig);

        container.items = [dateTimeSeparator, hoursField, separator, minutesField, secondsSeparator, secondsField];
        me.items = [dateField, container];


        me.callParent(arguments);

        if (me.value) {
            me.setValue(me.value);
        }
    },

    fireBlurEvent: function (field, event) {
        if (Ext.isEmpty(event) || Ext.isEmpty(event.target)) {
            return;
        }
        var me = this,
            dateField = me.down('#date-time-field-date'),
            hoursField = me.down('#date-time-field-hours'),
            minutesField = me.down('#date-time-field-minutes'),
            secondsField = me.down('#date-time-field-seconds');

        if (!dateField.getEl().contains(event.target) && !hoursField.getEl().contains(event.target) && !minutesField.getEl().contains(event.target)) {
            me.fireEvent('blur', me, me.getValue());
        }

    },

    formatDisplayOfTime: function (value) {
        var me = this,
            result = '00';

        if (Ext.isNumber(value)) {
            if (value < 10 && value > 0) {
                result = '0' + value;
            } else if (value >= 10) {
                result = value;
            }
            return result;
        }
        else if (me.allowNoValue) {
            return '';
        }
        return result;
    },

    numberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
        this.fireBlurEvent.apply(this, arguments);
    },

    setValue: function (value) {
        var me = this,
            dateField = me.down('#date-time-field-date'),
            hoursField = me.down('#date-time-field-hours'),
            minutesField = me.down('#date-time-field-minutes'),
            secondsField = me.down('#date-time-field-seconds');
        if (!Ext.isEmpty(value) && Ext.isNumber(value)) {
            me.eachItem(function (item) {
                item.suspendEvent('change');
            });
            dateField.setValue(moment(value).startOf('day').toDate());
            hoursField.setValue(moment(value).hours());
            minutesField.setValue(moment(value).minutes());
            secondsField.setValue(moment(value).seconds());
            me.fireEvent('change', me, value);
            me.eachItem(function (item) {
                item.resumeEvent('change');
            });
        } else {
            dateField.reset();
            hoursField.reset();
            minutesField.reset();
            secondsField.reset();
        }
    },

    getValueWithValidation: function () {
        var me = this,
            date = me.down('#date-time-field-date').getValue(),
            hours = me.down('#date-time-field-hours').getValue(),
            minutes = me.down('#date-time-field-minutes').getValue(),
            seconds=me.down('#date-time-field-seconds').getValue();

        if (Ext.isDate(date) && Ext.isNumber(hours) && Ext.isNumber(minutes) && Ext.isNumber(seconds)) {
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
            date = date.getTime();
            if (hours) {
                date += hours * 3600000;
            }
            if (minutes) {
                date += minutes * 60000;
            }
            if (seconds) {
                date += seconds * 6000;
            }
            if (me.valueInMilliseconds) {
                return date;
            }
            date = new Date(date);
            return me.submitFormat ? Ext.Date.format(date, me.submitFormat) : date;
        } else if (!Ext.isDate(date)) {
            me.down('#date-time-field-date').setValue(null);
            return null;
        }
        return null;
    },

    getValue: function () {
        var me = this,
            date = me.down('#date-time-field-date').getValue(),
            hours = me.down('#date-time-field-hours').getValue(),
            minutes = me.down('#date-time-field-minutes').getValue(),
            seconds=me.down('#date-time-field-seconds').getValue();

        if (Ext.isDate(date)) {
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
            date = date.getTime();
            if (hours) {
                date += hours * 3600000;
            }
            if (minutes) {
                date += minutes * 60000;
            }
            if (me.valueInMilliseconds) {
                return date;
            }
            date = new Date(date);
            return me.submitFormat ? Ext.Date.format(date, me.submitFormat) : date;
        } else {
            me.down('#date-time-field-date').setValue(null);
            return null;
        }
    },

    getRawValue: function () {
        return this.getValue().toString();
    },

    markInvalid: function (fields) {
        this.eachItem(function (field) {
            if (_.isFunction(field.markInvalid)) {
                field.markInvalid('');
            }
        });
        this.items.items[0].markInvalid(fields);
    },

    eachItem: function (fn, scope) {
        if (this.items && this.items.each) {
            this.items.each(fn, scope || this);
        }
    },

    onItemChange: function () {
        this.fireEvent('change', this, this.getValue());
    },

    setMinOrMaxValue: function (value, action) {
        var me = this,
            dateField = me.down('#date-time-field-date'),
            hoursField = me.down('#date-time-field-hours'),
            minutesField = me.down('#date-time-field-minutes'),
            secondsField = me.down('#date-time-field-seconds');

        if (value != null && Ext.isDate(new Date(value))) {
            dateField[action](moment(value).startOf('day').toDate());
            hoursField[action](moment(value).hours());
            minutesField[action](moment(value).minutes());
            secondsField[action](moment(value).seconds());
        } else {
            dateField[action](null);
            hoursField[action](null);
            minutesField[action](null);
            secondsField[action](null);
        }
    },

    setMinValue: function (value) {
        var me = this;

        me.setMinOrMaxValue(value, 'setMinValue');
    },

    setMaxValue: function (value) {
        var me = this;

        me.setMinOrMaxValue(value, 'setMaxValue');
    }
});