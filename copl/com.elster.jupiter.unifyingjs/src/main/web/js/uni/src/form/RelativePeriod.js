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
    noPreviewDateErrorMsg: 'It was not possible to calculate the preview date.',

    previewUrl: '/api/tmr/relativeperiods/preview',

    formatPreviewTextFn: function (dateString) {
        return Uni.I18n.translate(
            'form.relativeperiod.previewText',
            'UNI',
            'The datetime of the relative period is {0}.',
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

        me.updatePeriodFields(me.getValue().freqAgo);
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
                fieldLabel: 'Preview',
                items: [
                    {
                        xtype: 'label',
                        itemId: 'preview-label',
                        text: '',
                        cls: Ext.baseCSSPrefix + 'form-cb-label'
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

        me.updatePeriodFields(value.freqAgo);
        me.updatePreview();
    },

    updatePeriodFields: function (frequency) {
        var me = this,
            onField = me.getOnPeriodField(),
            atField = me.getAtPeriodField(),
            atHourField = atField.getHourField(),
            atMinuteField = atField.getMinuteField();

        onField.setOptionCurrentDisabled(frequency !== 'month');
        onField.setOptionDayOfMonthDisabled(frequency !== 'month');
        onField.setOptionDayOfWeekDisabled(frequency !== 'week');

        atHourField.setDisabled(frequency === 'hour' || frequency === 'minute');
        atMinuteField.setDisabled(frequency === 'minute');
    },

    updatePreview: function () {
        var me = this,
            label = me.down('#preview-label'),
            dateString = me.noPreviewDateErrorMsg;

        label.setText('');
        label.mask();

        Ext.Ajax.request({
            url: me.previewUrl,
            method: 'PUT',
            jsonData: me.formatJsonPreviewRequest(),
            success: function () {
                if (typeof me.previewDate !== 'undefined') {
                    dateString = Uni.I18n.formatDate('datetime.longdate', me.previewDate, 'UNI', 'l F j, Y \\a\\t H:i a');
                    dateString = me.formatPreviewTextFn(dateString);
                }
            },
            failure: function (response) {
                // Already caught be the default value of the date string.
            },
            callback: function () {
                label.setText(dateString);
                label.unmask();
            }
        });
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

    getValue: function () {
        var me = this,
            result = {},
            startValue = me.getStartPeriodField().getValue(),
            onValue = me.getOnPeriodField().getValue(),
            atValue = me.getAtPeriodField().getValue();

        Ext.apply(result, startValue);
        Ext.apply(result, onValue);
        Ext.apply(result, atValue);

        return result;
    }
});