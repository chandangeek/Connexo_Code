/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filtertop.Interval
 */
Ext.define('Uni.grid.filtertop.Interval', {
    extend: 'Ext.container.Container',
    xtype: 'uni-grid-filtertop-interval',

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    dataIndexFrom: null,
    dataIndexTo: null,
    defaultFromDate: undefined,
    defaultToDate: undefined,
    originalDefaultFromDate: undefined,
    originalDefaultToDate: undefined,
    originalTitle: null,
    withoutTime: false,
    minValue: undefined,
    fromAndToValueRequired: false,
    maxValue: undefined,

    initComponent: function () {
        var me = this;

        me.originalDefaultFromDate = Ext.clone(me.defaultFromDate);
        me.originalDefaultToDate = Ext.clone(me.defaultToDate);
        me.originalTitle = me.text;
        me.items = [
            {
                xtype: 'button',
                action: 'chooseInterval',
                text: me.text || Uni.I18n.translate('grid.filter.interval.label', 'UNI', 'Interval'),
                style: 'margin-right: 0 !important;',
                textAlign: 'left',
                width: 181,
                menu: [
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'uni-interval-form',
                        padding: '0 0 -8 0',
                        style: 'background-color: white;',
                        layout: {
                            type: 'vbox',
                            align: 'stretchmax'
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                margins: '4 8 0 10',
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
                                        xtype: 'label',
                                        html: me.text
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                margins: '8 8 0 8',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch',
                                    pack: 'center'
                                },
                                ui: 'small',
                                items: [
                                    {
                                        xtype: 'label',
                                        html: Uni.I18n.translate('general.from', 'UNI', 'From'),
                                        width: 48,
                                        style: 'font-weight: normal;'
                                    },
                                    {
                                        xtype: 'datefield',
                                        minValue: me.minValue,
                                        maxValue: me.maxValue,
                                        itemId: 'fromDate',
                                        editable: false,
                                        value: me.defaultFromDate,
                                        margins: '0 1 0 0',
                                        format: Uni.util.Preferences.lookup(Uni.DateTime.dateLongKey, Uni.DateTime.dateLongDefault),
                                        emptyText: Uni.I18n.translate('grid.filter.date.datefield.emptytext', 'UNI', 'Select a date'),
                                        width: 129,
                                        listeners: {
                                            'select' : function() {
                                                me.updateClearButton();
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                margins: '0 8 0 8',
                                hidden: me.withoutTime,
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
                                        itemId: 'fromHour',
                                        value: me.defaultFromDate?me.defaultFromDate.getHours():undefined,
                                        minValue: 0,
                                        maxValue: 23,
                                        maxLength: 2,
                                        enforceMaxLength: true,
                                        allowExponential: false,
                                        allowDecimals: false,
                                        editable: true,
                                        emptyText: Uni.I18n.translate('grid.filter.date.hourfield.emptytext', 'UNI', '00'),
                                        valueToRaw: function (value) {
                                            if (!Ext.isDefined(value)) {
                                                return null;
                                            }
                                            value = value || 0;
                                            return (value < 10 ? '0' : '') + value;
                                        },
                                        width: 57,
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
                                        itemId: 'fromMinute',
                                        value: me.defaultFromDate?me.defaultFromDate.getMinutes():undefined,
                                        minValue: 0,
                                        maxValue: 59,
                                        maxLength: 2,
                                        enforceMaxLength: true,
                                        allowExponential: false,
                                        allowDecimals: false,
                                        editable: true,
                                        emptyText: Uni.I18n.translate('grid.filter.date.minutefield.emptytext', 'UNI', '00'),
                                        valueToRaw: function (value) {
                                            if (!Ext.isDefined(value)) {
                                                return null;
                                            }
                                            value = value || 0;
                                            return (value < 10 ? '0' : '') + value;
                                        },
                                        width: 57,
                                        listeners: {
                                            blur: {
                                                fn: me.numberFieldValidation,
                                                scope: me
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                margins: '0 8 0 8',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch',
                                    pack: 'center'
                                },
                                items: [
                                    {
                                        xtype: 'label',
                                        html: Uni.I18n.translate('general.to', 'UNI', 'To'),
                                        width: 48,
                                        style: 'font-weight: normal;'
                                    },
                                    {
                                        xtype: 'datefield',
                                        minValue: me.minValue,
                                        maxValue: me.maxValue,
                                        itemId: 'toDate',
                                        editable: false,
                                        value: me.defaultToDate,
                                        margins: '0 1 0 0',
                                        format: Uni.util.Preferences.lookup(Uni.DateTime.dateLongKey, Uni.DateTime.dateLongDefault),
                                        emptyText: Uni.I18n.translate('grid.filter.date.datefield.emptytext', 'UNI', 'Select a date'),
                                        width: 129,
                                        listeners: {
                                            'select' : function() {
                                                me.updateClearButton();
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                margins: '0 8 0 8',
                                hidden: me.withoutTime,
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
                                        itemId: 'toHour',
                                        value: me.defaultToDate?me.defaultToDate.getHours():undefined,
                                        minValue: 0,
                                        maxValue: 23,
                                        maxLength: 2,
                                        enforceMaxLength: true,
                                        allowExponential: false,
                                        allowDecimals: false,
                                        editable: true,
                                        emptyText: Uni.I18n.translate('grid.filter.date.hourfield.emptytext', 'UNI', '00'),
                                        valueToRaw: function (value) {
                                            if (!Ext.isDefined(value)) {
                                                return null;
                                            }
                                            value = value || 0;
                                            return (value < 10 ? '0' : '') + value;
                                        },
                                        width: 57,
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
                                        itemId: 'toMinute',
                                        value: me.defaultToDate?me.defaultToDate.getMinutes():undefined,
                                        minValue: 0,
                                        maxValue: 59,
                                        maxLength: 2,
                                        enforceMaxLength: true,
                                        allowExponential: false,
                                        allowDecimals: false,
                                        editable: true,
                                        emptyText: Uni.I18n.translate('grid.filter.date.minutefield.emptytext', 'UNI', '00'),
                                        valueToRaw: function (value) {
                                            if (!Ext.isDefined(value)) {
                                                return null;
                                            }
                                            value = value || 0;
                                            return (value < 10 ? '0' : '') + value;
                                        },
                                        width: 57,
                                        listeners: {
                                            blur: {
                                                fn: me.numberFieldValidation,
                                                scope: me
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                margins: '0 8 0 60',
                                itemId: 'interval-error-msg',
                                cls: 'x-form-invalid-under',
                                hidden: true,
                                height: 25
                            },
                            {
                                xtype: 'fieldcontainer',
                                margins: '0 8 0 8',
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
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'apply',
                                        text: Uni.I18n.translate('general.apply', 'UNI', 'Apply'),
                                        disabled: me.fromAndToValueRequired ? (me.defaultFromDate && me.defaultToDate ? false : true) : false
                                    },
                                    {
                                        xtype: 'button',
                                        action: 'clear',
                                        text: Uni.I18n.translate('general.clear', 'UNI', 'Clear')
                                    }
                                ]
                            }
                        ],
                        markInvalid: function (msg) {
                            var errorMsg = this.down('#interval-error-msg');
                            errorMsg.update(msg);
                            errorMsg.show();
                        },
                        clearInvalid: function () {
                            this.down('#interval-error-msg').hide();
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.updateClearButton();
        me.updateTitle();
        me.initActions();
        me.updateTitle();
    },

    initActions: function () {
        var me = this,
            applyButton = me.down('button[action=apply]'),
            clearButton = me.down('button[action=clear]');

        applyButton.on('click', me.onApplyInterval, me);
        clearButton.on('click', me.onClearInterval, me);
        me.getFromDateField().on('change', me.onFromChanged, me);
        me.getToDateField().on('change', me.onToChanged, me);
    },

    onApplyInterval: function () {
        var me = this;

        if (me.isIntervalValid()) {
            me.fireFilterUpdateEvent();
            me.getChooseIntervalButton().hideMenu();
            me.updateTitle();
            me.updateClearButton();
        }
    },

    isIntervalValid: function() {
        var me = this;
        if ( me.getToDateValue() < me.getFromDateValue() ) {
            me.getIntervalForm().markInvalid(
                Uni.I18n.translate('interval.invalid', 'UNI', "The 'From' date must be before the 'To' date"));
            return false;
        }
        me.getIntervalForm().clearInvalid();
        return true;
    },

    onFromChanged: function() {
        var me = this,
            date = me.getFromDateField() ? me.getFromDateField().getValue() : undefined;
        if (date !== undefined) {
            me.getToDateField().setMinValue(date);
        }
    },

    onToChanged: function() {
        var me = this,
            date = me.getToDateField() ? me.getToDateField().getValue() : undefined;
        if (date !== undefined) {
            me.getFromDateField().setMaxValue(date);
        }
    },

    onClearInterval: function () {
        var me = this;

        me.getIntervalForm().clearInvalid();
        me.resetValue();
        me.fireFilterUpdateEvent();
        me.getChooseIntervalButton().hideMenu();
        me.updateTitle();
        me.updateClearButton();
    },

    resetValue: function () {
        var me = this;

        me.getIntervalForm().clearInvalid();

        me.getFromDateField().reset();
        if (!me.withoutTime) {
            me.getFromHourField().reset();
            me.getFromMinuteField().reset();
        }
        me.getFromDateField().setMaxValue(null);

        me.getToDateField().reset();
        if (!me.withoutTime) {
            me.getToHourField().reset();
            me.getToMinuteField().reset();
        }
        me.getToDateField().setMinValue(null);

        me.updateTitle();
        me.fireEvent('filtervaluechange');
    },

    setFilterValue: function (data) {
        var me = this;

        if (typeof data !== 'undefined') {
            var tokens = data.split('-'),
                fromDate = tokens[0],
                toDate = tokens[1];

            if (fromDate) {
                me.setFromDateValue(new Date(parseInt(fromDate)));
            }
            if (toDate) {
                me.setToDateValue(new Date(parseInt(toDate)));
            }
        }
        me.updateTitle();
        me.fireEvent('filtervaluechange');
    },

    getParamValue: function () {
        var me = this,
            fromValue = me.getFromDateValue(),
            toValue = me.getToDateValue();

        if (!Ext.isDefined(fromValue) && !Ext.isDefined(toValue)) {
            return undefined;
        }

        var result = '';
        if (Ext.isDefined(fromValue)) {
            result += fromValue;
        }
        result += '-';
        if (Ext.isDefined(toValue)) {
            result += toValue;
        }
        return result;
    },

    applyParamValue: function (params, includeUndefined, flattenObjects) {
        var me = this,
            fromValue = me.getFromDateValue(),
            toValue = me.getToDateValue();

        if (Ext.isDefined(fromValue)) {
            params[me.dataIndexFrom] = fromValue;
        } else if (includeUndefined) {
            params[me.dataIndexFrom] = undefined;
        }
        if (Ext.isDefined(toValue)) {
            params[me.dataIndexTo] = toValue;
        } else if (includeUndefined) {
            params[me.dataIndexTo] = undefined;
        }
        me.updateTitle();
        me.updateClearButton();
    },

    setFromDateValue: function (date) {
        var me = this,
            hours = date.getHours(),
            minutes = date.getMinutes();

        me.getFromDateField().setValue(date);
        if (!me.withoutTime) {
            me.getFromHourField().setValue(hours);
            me.getFromMinuteField().setValue(minutes);
        }
    },

    getFromDateValue: function () {
        var me = this,
            date = me.getFromDateField() ? Ext.clone(me.getFromDateField().getValue()) : undefined,
            hours = me.getFromHourField() ? Ext.clone(me.getFromHourField().getValue()) : undefined,
            minutes = me.getFromMinuteField() ? Ext.clone(me.getFromMinuteField().getValue()) : undefined;

        return me.createDateFromValues(date, hours, minutes);
    },

    setToDateValue: function (date) {
        var me = this,
            hours = date.getHours(),
            minutes = date.getMinutes();

        me.getToDateField().setValue(date);
        if (!me.withoutTime) {
            me.getToHourField().setValue(hours);
            me.getToMinuteField().setValue(minutes);
        }
    },

    getToDateValue: function () {
        var me = this,
            date = me.getToDateField() ? Ext.clone(me.getToDateField().getValue()) : undefined,
            hours = me.getToHourField() ? Ext.clone(me.getToHourField().getValue()) : undefined,
            minutes = me.getToMinuteField() ? Ext.clone(me.getToMinuteField().getValue()) : undefined;

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

    updateClearButton: function() {
        var me = this,
            currentFromDate = me.getFromDateValue(),
            currentToDate = me.getToDateValue();

        if ( ( (Ext.isEmpty(me.originalDefaultFromDate) && Ext.isEmpty(currentFromDate))
                ||
                (!Ext.isEmpty(me.originalDefaultFromDate) && !Ext.isEmpty(currentFromDate) && me.originalDefaultFromDate.getTime() === currentFromDate)
            )
            &&
            ( (Ext.isEmpty(me.originalDefaultToDate) && Ext.isEmpty(currentToDate))
                ||
                (!Ext.isEmpty(me.originalDefaultToDate) && !Ext.isEmpty(currentToDate) && me.originalDefaultToDate.getTime() === currentToDate)
            )
        ) {
            me.getClearButton().setDisabled(true);
        } else {
            me.getClearButton().setDisabled(false);
        }

        if (me.fromAndToValueRequired) {
            me.getApplyButton().setDisabled( Ext.isEmpty(currentFromDate) || Ext.isEmpty(currentToDate) );
        }
    },

    getChooseIntervalButton: function () {
        return this.down('button[action=chooseInterval]');
    },

    getFromDateField: function () {
        return this.down('datefield#fromDate');
    },

    getFromHourField: function () {
        return this.down('numberfield#fromHour');
    },

    getFromMinuteField: function () {
        return this.down('numberfield#fromMinute');
    },

    getToDateField: function () {
        return this.down('datefield#toDate');
    },

    getToHourField: function () {
        return this.down('numberfield#toHour');
    },

    getToMinuteField: function () {
        return this.down('numberfield#toMinute');
    },

    getIntervalForm: function() {
        return this.down('#uni-interval-form');
    },

    getClearButton: function () {
        return this.down('button[action=clear]');
    },

    getApplyButton: function () {
        return this.down('button[action=apply]');
    },

    updateTitle: function() {
        var me = this,
            fromValue = me.getFromDateValue(),
            toValue = me.getToDateValue(),
            fromDate,
            toDate;

        if (Ext.isDefined(fromValue)) {
            fromDate = me.withoutTime ? Uni.DateTime.formatDateShort(new Date(fromValue)) : Uni.DateTime.formatDateTimeShort(new Date(fromValue));
        }
        if (Ext.isDefined(toValue)) {
            toDate = me.withoutTime ? Uni.DateTime.formatDateShort(new Date(toValue)) : Uni.DateTime.formatDateTimeShort(new Date(toValue));
        }

        if (Ext.isDefined(fromValue) && Ext.isDefined(toValue)) {
            me.down('button').setText(fromDate + ' / ' + toDate);
        } else if (Ext.isDefined(fromValue)) {
            me.down('button').setText(fromDate + ' / *');
        } else if (Ext.isDefined(toValue)) {
            me.down('button').setText('* / ' + toDate);
        } else {
            me.down('button').setText( me.originalTitle );
        }
    },

    numberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue || value === 0) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    }

});