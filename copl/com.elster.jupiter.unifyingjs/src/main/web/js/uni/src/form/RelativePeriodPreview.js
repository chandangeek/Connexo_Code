/**
 * @class Uni.form.RelativePeriodPreview
 */
Ext.define('Uni.form.RelativePeriodPreview', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-form-relativeperiodpreview',

    requires: [
    ],

    /**
     * @cfg noPreviewDateErrorMsg
     *
     * Message shown in the preview when no preview date has been defined.
     */
    noPreviewDateErrorMsg: 'It was not possible to calculate the preview dates.',

    previewUrl: '/api/tmr/relativeperiods/preview',

    /**
     *
     */
    startValue: undefined,

    /**
     *
     */
    endValue: undefined,

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

        // TODO
    },

    formatJsonPreviewRequest: function () {
        var me = this,
            date = new Date(),
            value = me.getValue();

        return {
            date: date,
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