/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filtertop.Date
 */
Ext.define('Uni.grid.filtertop.DateTime', {
    extend: 'Ext.container.Container',
    xtype: 'uni-grid-filtertop-datetime',

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    editable: false,
    value: undefined,
    emptyText: Uni.I18n.translate('grid.filter.date.label', 'UNI', 'Date'),
    format: Uni.util.Preferences.lookup(Uni.DateTime.dateLongKey, Uni.DateTime.dateLongDefault),
    labelText: null,
    layout: {
        type: 'vbox',
        align: 'stretchmax'
    },
    ui: 'small',
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'fieldcontainer',
                layout: {
                    type: 'hbox',
                    align: 'stretch',
                    pack: 'center'
                },
                items: [
                    {
                        xtype: 'label',
                        html: me.labelText || Uni.I18n.translate('general.from', 'UNI', 'From'),
                        width: 48,
                        style: 'font-weight: normal;'
                    },
                    {
                        xtype: 'datefield',
                        itemId: 'date',
                        editable: false,
                        value: me.value,
                        margins: '0 1 0 0',
                        format: me.format,
                        emptyText: Uni.I18n.translate('grid.filter.date.datefield.emptytext', 'UNI', 'Select a date'),
                        width: 129
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                layout: {
                    type: 'column',
                    align: 'stretch',
                    pack: 'center'
                },
                items: [
                    {
                        xtype: 'label',
                        html: '&nbsp;',
                        width: 48
                    },
                    {
                        xtype: 'numberfield',
                        itemId: 'hour',
                        value: me.value ? me.value.getHours() : undefined,
                        minValue: 0,
                        maxValue: 23,
                        maxLength: 2,
                        enforceMaxLength: true,
                        allowExponential: false,
                        allowDecimals: false,
                        editable: true,
                        emptyText: Uni.I18n.translate('grid.filter.date.hourfield.emptytext', 'UNI', '00'),
                        width: 57,
                        valueToRaw: function (value) {
                            if (!Ext.isDefined(value)) {
                                return null;
                            }
                            value = value || 0;
                            return (value < 10 ? '0' : '') + value;
                        },
                        listeners: {
                            blur: {
                                fn: me.numberFieldValidation,
                                scope: me
                            }
                        }
                    },
                    {
                        xtype: 'label',
                        text: ':',
                        margin: '6 6 0 6'
                    },
                    {
                        xtype: 'numberfield',
                        itemId: 'minute',
                        value: me.value ? me.value.getMinutes() : undefined,
                        minValue: 0,
                        maxValue: 59,
                        maxLength: 2,
                        enforceMaxLength: true,
                        allowExponential: false,
                        allowDecimals: false,
                        editable: true,
                        emptyText: Uni.I18n.translate('grid.filter.date.hourfield.emptytext', 'UNI', '00'),
                        width: 57,
                        valueToRaw: function (value) {
                            if (!Ext.isDefined(value)) {
                                return null;
                            }
                            value = value || 0;
                            return (value < 10 ? '0' : '') + value;
                        },
                        listeners: {
                            blur: {
                                fn: me.numberFieldValidation,
                                scope: me
                            }
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    numberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue || value === 0) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    },

    resetValue: function () {
        var me = this;
        me.getDateField().reset();
        me.getHourField().reset();
        me.getMinuteField().reset();
    },

    setFilterValue: function (data) {
        var me = this;

        if (Ext.isDate(data)) {
            me.setDateValue(data);
        } else if (typeof data !== 'undefined') {
            me.setDateValue(new Date(parseInt(data)));
        }
    },

    getParamValue: function () {
        return this.getDateValue();
    },

    applyParamValue: function (params, includeUndefined, flattenObjects) {
        var me = this,
            date = me.getDateValue();

        if (!includeUndefined && Ext.isDefined(date)) {
            params[me.dataIndex] = date;
        } else if (!Ext.isDefined(date)) {
            params[me.dataIndex] = undefined;
        }
    },

    setDateValue: function (date) {
        var me = this,
            hours = date.getHours(),
            minutes = date.getMinutes();

        me.getDateField().setValue(date);
        me.getHourField().setValue(hours);
        me.getMinuteField().setValue(minutes);
    },

    getDateValue: function () {
        var me = this,
            date = me.getDateField() ? me.getDateField().getValue() : undefined,
            hours = me.getHourField() ? me.getHourField().getValue() : undefined,
            minutes = me.getMinuteField() ? me.getMinuteField().getValue() : undefined;

        return me.createDateFromValues(date, hours, minutes);
    },

    createDateFromValues: function (date, hours, minutes) {
        if (Ext.isDefined(date) && !Ext.isEmpty(date)) {
            hours = typeof hours === 'undefined' ? 0 : hours;
            minutes = typeof minutes === 'undefined' ? 0 : minutes;

            date.setHours(hours);
            date.setMinutes(minutes);
            date.setSeconds(0);
            date.setMilliseconds(0);

            return date.getTime();
        }

        return undefined;
    },

    getDateField: function () {
        return this.down('datefield#date');
    },

    getHourField: function () {
        return this.down('numberfield#hour');
    },

    getMinuteField: function () {
        return this.down('numberfield#minute');
    }
});