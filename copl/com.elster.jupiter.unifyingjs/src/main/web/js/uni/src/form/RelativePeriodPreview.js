/**
 * @class Uni.form.RelativePeriodPreview
 */
Ext.define('Uni.form.RelativePeriodPreview', {
    extend: 'Ext.container.Container',
    xtype: 'uni-form-relativeperiodpreview',

    requires: [
        'Uni.form.field.DateTime'
    ],

    /**
     * @cfg noPreviewDateErrorMsg
     *
     * Message shown in the preview when no preview date has been defined.
     */
    noPreviewDateErrorMsg: Uni.I18n.translate('form.relativePeriod.errorMsg', 'UNI', 'It was not possible to calculate the preview date.'),

    previewUrl: '/api/tmr/relativeperiods/preview',

    /**
     * @cfg startPeriodValue
     */
    startPeriodValue: undefined,

    startPeriodDate: undefined,

    /**
     * @cfg endPeriodValue
     */
    endPeriodValue: undefined,

    endPeriodDate: undefined,

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
                        text: 'at',
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
                        value: 0,
                        minValue: 0,
                        maxValue: 23,
                        allowBlank: false,
                        width: 64,
                        margin: '0 6 0 6'
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
                        value: 0,
                        minValue: 0,
                        maxValue: 59,
                        allowBlank: false,
                        width: 64,
                        margin: '0 6 0 6'
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('relativeperiod.form.referencedate.tooltip', 'UNI', 'Select a reference date to evaluate the relative period.'),
                        iconCls: 'uni-icon-info-small',
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

        if (typeof me.startPeriodValue !== 'undefined' && typeof me.endPeriodValue !== 'undefined') {
            me.startPeriodDate = undefined;
            me.endPeriodDate = undefined;

            Ext.Ajax.request({
                url: me.previewUrl,
                method: 'PUT',
                jsonData: me.formatJsonPreviewRequest(me.startPeriodValue),
                success: function (response, data) {
                    var json = Ext.decode(response.responseText, true);
                    var dateLong = json.date;
                    var zoneOffset = json.zoneOffset;
                    if (typeof dateLong !== 'undefined') {
                        var startDate = new Date(dateLong);
                        var startDateUtc = startDate.getTime() + (startDate.getTimezoneOffset() * 60000);
                        var zonedDate = new Date(startDateUtc - (60000*zoneOffset));
                        me.startPeriodDate = new Date(zonedDate);
                    }

                    me.updatePreviewLabel(me.startPeriodDate, me.endPeriodDate);
                },
                failure: function (response) {
                    me.getPreviewLabel().update(dateString);
                }
            });

            Ext.Ajax.request({
                url: me.previewUrl,
                method: 'PUT',
                jsonData: me.formatJsonPreviewRequest(me.endPeriodValue),
                success: function (response, data) {
                    var json = Ext.decode(response.responseText, true);
                    var dateLong = json.date;
                    var zoneOffset = json.zoneOffset;
                    if (typeof dateLong !== 'undefined') {
                        var startDate = new Date(dateLong);
                        var startDateUtc = startDate.getTime() + (startDate.getTimezoneOffset() * 60000);
                        var zonedDate = new Date(startDateUtc - (60000*zoneOffset));
                        me.endPeriodDate = new Date(zonedDate);
                    }

                    me.updatePreviewLabel(me.startPeriodDate, me.endPeriodDate);
                },
                failure: function (response) {
                    me.getPreviewLabel().update(dateString);
                }
            });
        }
    },

    updatePreviewLabel: function (startDate, endDate) {
        var me = this,
            startDateString = Uni.I18n.translate('general.dateattime', 'UNI', '{0} At {1}',[Uni.DateTime.formatDateLong(startDate), Uni.DateTime.formatTimeLong(startDate)]).toLowerCase(),
            endDateString = Uni.I18n.translate('general.dateattime', 'UNI', '{0} At {1}',[Uni.DateTime.formatDateLong(endDate), Uni.DateTime.formatTimeLong(endDate)]).toLowerCase(),
            dateString = me.formatPreviewTextFn(startDateString, endDateString);

        if (typeof startDate !== 'undefined' && typeof endDate !== 'undefined') {
            me.getPreviewLabel().update(dateString);
            me.getPreviewLabel().unmask();
        }
    },

    updateStartPeriodValue: function (startPeriodValue) {
        this.startPeriodValue = startPeriodValue;
    },

    updateEndPeriodValue: function (endPeriodValue) {
        this.endPeriodValue = endPeriodValue;
    },

    formatPreviewTextFn: function (startDateString, endDateString) {
        return Uni.I18n.translate(
            'form.relativeperiodpreview.previewText',
            'UNI',
            'From {0} to {1}.',
            [startDateString, endDateString]
        );
    },

    formatJsonPreviewRequest: function (periodValue) {
        var me = this,
            date = me.getValue();

        var result = {
            date: date.getTime(),
            zoneOffset: date.getTimezoneOffset(),
            relativeDateInfo: periodValue
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
    }
});