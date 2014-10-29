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
                xtype: 'component',
                itemId: 'preview-label',
                html: me.noPreviewDateErrorMsg,
                cls: Ext.baseCSSPrefix + 'form-item-label',
                style: {
                    fontWeight: 'normal'
                },
                margin: '8 0 8 0'
            },
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'label',
                        text: 'The relative period is defined using',
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'datefield',
                        allowBlank: false,
                        value: new Date(),
                        width: 128,
                        margin: '0 6 0 6'
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
                        xtype: 'label',
                        text: 'as reference',
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('relativeperiod.form.referencedete.tooltip', 'TME', 'You can change the reference to define another relative period'),
                        iconCls: 'icon-info-small',
                        ui: 'blank',
                        itemId: 'latestReadingHelp',
                        shadow: false,
                        margin: '6 0 0 6',
                        width: 16
                    }
                ]
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
                    var dateLong = data.jsonData ? data.jsonData.date : undefined;

                    if (typeof dateLong !== 'undefined') {
                        me.startPeriodDate = new Date(dateLong);
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
                    var dateLong = data.jsonData ? data.jsonData.date : undefined;

                    if (typeof dateLong !== 'undefined') {
                        me.endPeriodDate = new Date(dateLong);
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
            startDateString = Uni.I18n.formatDate('datetime.longdate', startDate, 'UNI', 'l F j, Y \\a\\t H:i a'),
            endDateString = Uni.I18n.formatDate('datetime.longdate', endDate, 'UNI', 'l F j, Y \\a\\t H:i a'),
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

        return {
            date: date.getTime(),
            zoneOffset: date.getTimezoneOffset(),
            relativeDateInfo: periodValue
        };
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