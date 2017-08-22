/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.RelativePeriod
 */
Ext.define('Uni.form.RelativePeriod', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-form-relativeperiod',

    requires: [
        'Uni.form.field.StartPeriod',
        'Uni.form.field.OnPeriod',
        'Uni.form.field.AtPeriod'
    ],

    /**
     * @cfg startPeriodCfg
     *
     * Custom config for the start period component.
     */
    startPeriodCfg: {},

    /**
     * @cfg noPreviewDateErrorMsg
     *
     * Message shown in the preview when no preview date has been defined.
     */
    noPreviewDateErrorMsg: Uni.I18n.translate('form.relativePeriod.errorMsg', 'UNI', 'It was not possible to calculate the preview date.'),

    previewUrl: '/api/tmr/relativeperiods/preview',
    suspend: false,

    formatPreviewTextFn: function (dateString) {
        return Uni.I18n.translate(
            'form.relativePeriod.previewText',
            'UNI',
            'The date and time of the relative period is {0}.',
            [dateString]
        );
    },

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);
        me.initListeners();

        me.on('afterrender', me.onAfterRender, me);
    },

    onAfterRender: function () {
        var me = this;

        me.updatePeriodFields(me.getValue().startPeriodAgo);
        me.updatePreview();
    },

    buildItems: function () {
        var me = this;

        me.items = [
            Ext.apply(
                {
                    xtype: 'uni-form-field-startperiod',
                    required: true
                },
                me.startPeriodCfg
            ),
            {
                xtype: 'uni-form-field-onperiod'
            },
            {
                xtype: 'uni-form-field-atperiod'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('form.relativePeriod.preview', 'UNI', 'Preview'),
                combineErrors: true,
                msgTarget: 'under',
                items: [
                    {
                        xtype: 'component',
                        itemId: 'preview-label',
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        },
                        html: ''
                    },
                    {
                        // added for validation
                        xtype: 'displayfield',
                        name: me.startPeriodCfg && me.startPeriodCfg.errorId
                            ? me.startPeriodCfg.errorId
                            : undefined,
                        hidden: true
                    }
                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getStartPeriodField().on('periodchange', me.onStartPeriodChange, me);
        me.getOnPeriodField().on('periodchange', me.updatePreview, me);
        me.getAtPeriodField().on('periodchange', me.updatePreview, me);
    },

    onStartPeriodChange: function (value) {
        var me = this;

        me.updatePeriodFields(value.startPeriodAgo);
        me.updatePreview();
    },

    updatePeriodFields: function (frequency) {
        var me = this,
            startField = me.getStartPeriodField(),
            useStartDate = startField.showOptionDate ? startField.getOptionDateRadio().getValue() : false,
            onField = me.getOnPeriodField(),
            atField = me.getAtPeriodField(),
            atHourField = atField.getHourField(),
            atMinuteField = atField.getMinuteField();

        var optionCurrentDisabled = frequency !== 'months' || useStartDate;
        var optionDayOfMonthDisabled = frequency !== 'months' || useStartDate;
        var optionDayOfWeekDisabled = frequency !== 'weeks' || useStartDate;
        var optionCurrentDayOfYearDisabled = frequency !== 'years' || useStartDate;
        var optionDayOfYearDisabled = frequency !== 'years' || useStartDate;

        onField.setOptionCurrentDisabled(optionCurrentDisabled);
        onField.setOptionDayOfMonthDisabled(optionDayOfMonthDisabled);
        onField.setOptionDayOfWeekDisabled(optionDayOfWeekDisabled);
        onField.setOptionCurrentOfYearDisabled(optionCurrentDayOfYearDisabled);
        onField.setOptionDayOfYearDisabled(optionDayOfYearDisabled);

        onField.setVisible(!optionCurrentDisabled || !optionDayOfMonthDisabled || !optionDayOfWeekDisabled || !optionCurrentDayOfYearDisabled || !optionDayOfYearDisabled);
        onField.refreshControls();

        var hourfieldVisible = !(frequency === 'hours' || frequency === 'minutes');
        atHourField.setVisible(hourfieldVisible);
        atHourField.setDisabled(!hourfieldVisible);
        me.getSeparatorField().setVisible(hourfieldVisible);
        var minutefieldVisible = !(frequency === 'minutes');
        atMinuteField.setVisible(minutefieldVisible);
        atMinuteField.setDisabled(!minutefieldVisible);
        me.getMinutesUnitField().setVisible(!hourfieldVisible && minutefieldVisible);
        var atVisible = hourfieldVisible || minutefieldVisible ? (startField.showOptionNow ? !startField.getOptionNowRadio().getValue() : true) : false;
        atField.setVisible(atVisible);

    },

    updatePreview: function () {
        var me = this,
            label = me.down('#preview-label'),
            dateString = me.noPreviewDateErrorMsg;

        if (!me.suspend) {
            me.fireEvent('periodchange', me.getValue());
            label.mask();

            Ext.Ajax.request({
                url: me.previewUrl,
                method: 'PUT',
                jsonData: me.formatJsonPreviewRequest(),
                success: function (response, data) {
                    var json = Ext.decode(response.responseText, true);
                    var dateLong = json.date;
                    var zoneOffset = json.zoneOffset;
                    if (typeof dateLong !== 'undefined') {
                        var startDate = new Date(dateLong);
                        var startDateUtc = startDate.getTime() + (startDate.getTimezoneOffset() * 60000);
                        var zonedDate = new Date(startDateUtc - (60000 * zoneOffset));
                        zonedDate.setSeconds(0);
                        dateString = Uni.DateTime.formatDateTimeLong(new Date(zonedDate));
                        dateString = me.formatPreviewTextFn(dateString);
                    }
                },
                failure: function (response) {
                    // Already caught be the default value of the date string.
                },
                callback: function () {
                    label.update(dateString);
                    label.unmask();
                }
            });
        }
    },

    setValues: function (relativePeriod) {
        var me = this,
            date = new Date(),
            onField = me.down('uni-form-field-onperiod');
        me.suspend = true;
        if (!Ext.isEmpty(relativePeriod.startFixedYear)) {
            date.setYear(relativePeriod.startFixedYear);
            date.setMonth(relativePeriod.startFixedMonth - 1);
            date.setDate(relativePeriod.startFixedDay);
            date.setHours(relativePeriod.atHour);
            date.setMinutes(relativePeriod.atMinute);
            me.down('uni-form-field-startperiod').down('#option-date').down('datefield').setValue(date);
        } else if (!Ext.isEmpty(relativePeriod.startPeriodAgo)) {
            me.down('uni-form-field-startperiod').down('#option-ago').down('numberfield').setValue(relativePeriod.startAmountAgo);
            me.down('uni-form-field-startperiod').down('#option-ago').down('#period-interval').setValue(relativePeriod.startPeriodAgo);
            me.down('uni-form-field-startperiod').down('#option-ago').down('#period-edge').setValue(relativePeriod.startTimeMode);
            me.down('uni-form-field-startperiod').down('#option-ago').down('radio').setValue(true);

            switch (relativePeriod.startPeriodAgo) {
                case 'years':
                    me.setHourAndMinutesField(relativePeriod);
                    if (Ext.isEmpty(relativePeriod.onCurrentDayOfYear)) {
                        onField.down('#option-doy').down('numberfield').setValue(relativePeriod.startFixedDay);
                        onField.down('#option-doy').down('combobox').setValue(relativePeriod.startFixedMonth);
                        onField.selectOptionDayOfYear(false);
                    }
                    break;
                case 'months':
                    me.setHourAndMinutesField(relativePeriod);
                    if (Ext.isEmpty(relativePeriod.onCurrentDay)) {
                        onField.selectedValue = 'dayofmonth';
                        onField.dom = true;
                        onField.down('#option-dom').down('combobox').setValue(relativePeriod.onDayOfMonth);
                        onField.selectOptionDayOfMonth(true);
                    }
                    break;
                case 'weeks':
                    me.setHourAndMinutesField(relativePeriod);
                    me.down('uni-form-field-onperiod').down('#option-dow-combo').setValue(relativePeriod.onDayOfWeek);
                    break;
                case 'days':
                    me.setHourAndMinutesField(relativePeriod);
                    break;
                case 'hours':
                    me.down('uni-form-field-atperiod').down('#minute-field').setValue(relativePeriod.atMinute);
                    break;
            }
        }
        me.suspend = false;
        me.updatePreview();
    },

    setHourAndMinutesField: function (relativePeriod) {
        var me = this;
        me.down('uni-form-field-atperiod').down('#hour-field').setValue(relativePeriod.atHour);
        me.down('uni-form-field-atperiod').down('#minute-field').setValue(relativePeriod.atMinute);
    },

    formatJsonPreviewRequest: function () {
        var me = this,
            date = new Date(),
            value = me.getValue();

        return {
            date: date.getTime(),
            zoneOffset: date.getTimezoneOffset(),
            relativeDateInfo: value
        };
    },

    getStartPeriodField: function () {
        return this.down('uni-form-field-startperiod');
    },

    getOnPeriodField: function () {
        return this.down('uni-form-field-onperiod');
    },

    getAtPeriodField: function () {
        return this.down('uni-form-field-atperiod');
    },

    getSeparatorField: function () {
        return this.down('uni-form-field-atperiod').down('#separator-field');
    },

    getMinutesUnitField: function () {
        return this.down('uni-form-field-atperiod').down('#minutes-unit-field');
    },

    getValue: function () {
        var me = this,
            result = {},
            startValue = me.getStartPeriodField().getStartValue(),
            onValue = me.getOnPeriodField().getOnValue(),
            atValue = me.getAtPeriodField().getValue();

        Ext.apply(result, startValue);
        Ext.apply(result, onValue);
        Ext.apply(result, atValue);

        return result;
    }
});