/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.RelativePeriodPreviewBasedOnId', {
    extend: 'Ext.container.Container',
    xtype: 'uni-form-relativeperiodpreview-basedOnId',

    requires: [
        'Uni.form.field.DateTime'
    ],

    /**
     * @cfg noPreviewDateErrorMsg
     *
     * Message shown in the preview when no preview date has been defined.
     */
    noPreviewDateErrorMsg: Uni.I18n.translate('form.relativePeriod.errorMsg', 'UNI', 'It was not possible to calculate the preview date.'),

    previewUrlTpl: '/api/tmr/relativeperiods/{0}/preview',
    relativePeriodId: undefined,
    previewUrl: undefined,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);
        me.initListeners();

        me.on('afterrender', me.onAfterRender, me);
    },

    onAfterRender: function () {
        var me = this;

        me.updatePreview();
    },

    buildItems: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('period.preview.base', 'UNI','Preview based on date:'),
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'datefield',
                        allowBlank: false,
                        editable: false,
                        value: new Date(),
                        width: 128,
                        margin: '0 6 0 6',
                        format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                    },
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('general.at.lowercase', 'UNI','at'),
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'numberfield',
                        itemId: 'hour-field',
                        hideLabel: true,
                        valueToRaw: me.formatDisplayOfTime,
                        value: moment().hours(),
                        minValue: 0,
                        maxValue: 23,
                        allowBlank: false,
                        width: 64,
                        margin: '0 6 0 6',
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
                        value: moment().minutes(),
                        minValue: 0,
                        maxValue: 59,
                        allowBlank: false,
                        width: 64,
                        margin: '0 6 0 6',
                        listeners: {
                            blur: {
                                fn: me.numberFieldValidation,
                                scope: me
                            }
                        }
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('relativeperiod.form.referencedate.tooltip', 'UNI', 'Select a reference date to evaluate the relative period.'),
                        text: '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                        disabled: true, // to avoid a hand cursor
                        ui: 'blank',
                        itemId: 'latestReadingHelp',
                        shadow: false,
                        margin: '6 0 0 6',
                        width: 16
                    }
                ]
            },
            {
                xtype: 'component',
                itemId: 'preview-label',
                html: Ext.String.htmlEncode(me.noPreviewDateErrorMsg),
                cls: Ext.baseCSSPrefix + 'form-item-label',
                style: {
                    fontWeight: 'normal'
                },
                margin: '15 0 3 0'
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getDateField().on('change', me.updatePreview, me);
        me.getHourField().on('change', me.updatePreview, me);
        me.getMinuteField().on('change', me.updatePreview, me);
    },

    updatePreview: function () {
        var me = this,
            label = me.getPreviewLabel(),
            dateString = me.noPreviewDateErrorMsg;

        label.mask();

        if (!Ext.isEmpty(me.relativePeriodId)) {
            Ext.Ajax.request({
                url: me.previewUrl,
                method: 'PUT',
                jsonData: me.formatJsonPreviewRequest(),
                success: function (response, data) {
                    var json = Ext.decode(response.responseText, true);
                    var startDateLong = json.start.date,
                        startDateOffset = json.start.zoneOffset,
                        endDateLong = json.end.date,
                        endDateOffset = json.end.zoneOffset,
                        startPeriodDate,
                        endPeriodDate;
                    if (typeof startDateLong !== 'undefined') {
                        var startDate = new Date(startDateLong),
                            startDateUtc = startDate.getTime() + (startDate.getTimezoneOffset() * 60000);
                        startPeriodDate = new Date(startDateUtc - (60000*startDateOffset));
                    }
                    if (typeof endDateLong !== 'undefined') {
                        var endDate = new Date(endDateLong),
                            endDateUtc = endDate.getTime() + (endDate.getTimezoneOffset() * 60000);
                        endPeriodDate = new Date(endDateUtc - (60000*endDateOffset));
                    }
                    me.updatePreviewLabel(startPeriodDate, endPeriodDate);
                },
                failure: function (response) {
                    me.getPreviewLabel().update(dateString);
                }
            });
        }
    },

    updatePreviewLabel: function (startDate, endDate) {
        if (Ext.isEmpty(this.getPreviewLabel())) return;
        if (typeof startDate !== 'undefined' && typeof endDate !== 'undefined') {
            var me = this,
                startDateString = Uni.DateTime.formatDateTimeLong(startDate),
                endDateString = Uni.DateTime.formatDateTimeLong(endDate),
                dateString = me.formatPreviewTextFn(startDateString, endDateString);

            me.getPreviewLabel().update(dateString);
            me.getPreviewLabel().unmask();
        }
    },

    setRelativePeriodId: function(relativePeriodId) {
        this.relativePeriodId = relativePeriodId;
        this.previewUrl = Ext.String.format(this.previewUrlTpl, relativePeriodId);
    },

    formatPreviewTextFn: function (startDateString, endDateString) {
        return Uni.I18n.translate(
            'form.relativeperiodpreview.previewText',
            'UNI',
            'From {0} to {1}.',
            [startDateString, endDateString]
        );
    },

    formatJsonPreviewRequest: function() {
        var me = this,
            date = me.getValue();

        var result = {
            date: date.getTime(),
            zoneOffset: date.getTimezoneOffset()
        };
        return result;
    },

    getPreviewLabel: function () {
        return this.down('#preview-label');
    },

    getDateField: function () {
        return this.down('datefield');
    },

    getHourField: function () {
        return this.down('#hour-field');
    },

    getMinuteField: function () {
        return this.down('#minute-field');
    },

    getValue: function () {
        var me = this,
            date = me.getDateField().getValue(),
            hours = me.getHourField().getValue(),
            minutes = me.getMinuteField().getValue();

        date.setHours(hours);
        date.setMinutes(minutes);
        date.setSeconds(0);
        date.setMilliseconds(0);

        return date;
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
        var me = this,
            value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    }

});