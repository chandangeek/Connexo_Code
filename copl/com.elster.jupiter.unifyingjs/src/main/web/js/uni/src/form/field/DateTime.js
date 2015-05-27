/**
 * @class Uni.form.field.DateTime
 *
 * This class contains the DateTime field.
 *
 *     Ext.create('Uni.form.field.DateTime', {
 *       itemId: 'endOfInterval',
 *       name: 'intervalStart',
 *       fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'),
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

    dateTimeSeparatorConfig: null,

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
                listeners: {
                    change: {
                        fn: me.onItemChange,
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
            separator = {
                xtype: 'component',
                html: ':',
                margin: '0 5 0 5'
            },
            dateTimeSeparator = {
                xtype: 'component',
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
                    value: 0,
                    valueToRaw: me.formatDisplayOfTime,
                    listeners: {
                        change: {
                            fn: me.onItemChange,
                            scope: me
                        },
                        blur: me.numberFieldValidation
                    }
                }
            };

        if (me.layout === 'hbox') {
            delete container.width;
            dateField.width = 130;
            hoursField.width = 80;
            minutesField.width = 80;
        }

        Ext.apply(dateField, me.dateConfig);
        Ext.apply(hoursField, me.hoursConfig);
        Ext.apply(minutesField, me.minutesConfig);
        Ext.apply(separator, me.separatorConfig);
        Ext.apply(dateTimeSeparator, me.dateTimeSeparatorConfig);

        container.items = [dateTimeSeparator, hoursField, separator, minutesField];
        me.items = [dateField, container];


        me.callParent(arguments);

        if (me.value) {
            me.setValue(me.value);
        }
    },

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
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    },

    setValue: function (value) {
        var me = this,
            dateField = me.down('#date-time-field-date'),
            hoursField = me.down('#date-time-field-hours'),
            minutesField = me.down('#date-time-field-minutes');
        if (value != null && Ext.isDate(new Date(value))) {
            me.eachItem(function (item) {
                item.suspendEvent('change');
            });
            dateField.setValue(moment(value).startOf('day').toDate());
            hoursField.setValue(moment(value).hours());
            minutesField.setValue(moment(value).minutes());
            me.fireEvent('change', me, value);
            me.eachItem(function (item) {
                item.resumeEvent('change');
            });
        } else {
            dateField.reset();
            hoursField.reset();
            minutesField.reset();
        }
    },

    getValue: function () {
        var me = this,
            date = me.down('#date-time-field-date').getValue(),
            hours = me.down('#date-time-field-hours').getValue(),
            minutes = me.down('#date-time-field-minutes').getValue();

        if (Ext.isDate(date)) {
            date = date.getTime();
            if (hours) date += hours * 3600000;
            if (minutes) date += minutes * 60000;
            if (me.getRawValue) {
                return date;
            } else {
                date = new Date(date);
                return me.submitFormat ? Ext.Date.format(date, me.submitFormat) : date;
            }
        } else {
            me.down('#date-time-field-date').setValue(null);
            return null;
        }
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
    }
});

