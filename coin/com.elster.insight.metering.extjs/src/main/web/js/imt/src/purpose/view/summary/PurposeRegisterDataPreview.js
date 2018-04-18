/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Imt.purpose.view.summary.PurposeRegisterDataPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.purpose-register-data-preview',

    requires: [
        'Imt.purpose.util.DataFormatter',
        'Imt.purpose.util.PreviewRenderer',
        'Cfg.view.field.ReadingQualities'
    ],

    initComponent: function () {
        var me = this,
            generalItems = me.getGeneralItems(),
            validationItems = me.getValidationItems(),
            qualityItems = me.getQualityItems();

        me.items = [
            {
                title: Uni.I18n.translate('reading.generaltab.titl', 'IMT', 'General'),
                items: {
                    xtype: 'form',
                    itemId: 'register-preview-general-panel',
                    frame: true,
                    align: 'stretch',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: generalItems
                }
            },
            {
                title: Uni.I18n.translate('general.validation', 'IMT', 'Validation'),
                items: {
                    xtype: 'form',
                    itemId: 'register-preview-validation-panel',
                    frame: true,
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: validationItems,
                    layout: 'vbox'
                }
            },
            {
                title: Uni.I18n.translate('general.readingQuality', 'IMT', 'Reading quality'),
                items: {
                    xtype: 'form',
                    itemId: 'register-preview-qualities-panel',
                    frame: true,
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200,
                        htmlEncode: false
                    },
                    items: qualityItems,
                    layout: 'vbox'
                }
            }
        ];
        me.callParent(arguments);
    },

    getGeneralItems: function () {
        var me = this;
        return [
            {
                fieldLabel: Uni.I18n.translate('general.measurementPeriod', 'IMT', 'Measurement period'),
                name: 'interval',
                itemId: 'register-preview-measurementPeriod-field',
                renderer: Imt.purpose.util.DataFormatter.formatIntervalLong
            },
            {
                fieldLabel: Uni.I18n.translate('general.measurementTime', 'IMT', 'Measurement time'),
                name: 'timeStamp',
                itemId: 'register-preview-measurementTime-field',
                renderer: Imt.purpose.util.DataFormatter.formatDateLong
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.eventTime', 'IMT', 'Event time'),
                name: 'eventDate',
                itemId: 'register-preview-eventTime-field',
                renderer: Imt.purpose.util.DataFormatter.formatDateLong
            },
            {
                fieldLabel: Uni.I18n.translate('general.value', 'IMT', 'Value'),
                name: 'value',
                itemId: 'register-preview-value-field',
                renderer: function(value, displayField){
                    return me.renderValueAndUnit(value, displayField)
                }
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.deltaValue', 'IMT', 'Delta value'),
                name: 'deltaValue',
                itemId: 'register-preview-deltaValue-field',
                renderer: function(value, displayField){
                    return me.renderValueAndUnit(value, displayField)
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.formula', 'IMT', 'Formula'),
                itemId: 'register-preview-formula-field'
            },
            {
                fieldLabel: Uni.I18n.translate('device.readingData.lastUpdate', 'IMT', 'Last update'),
                name: 'reportedDateTime',
                itemId: 'register-preview-lastUpdate-field',
                renderer: Imt.purpose.util.DataFormatter.formatDateLong
            }
        ];
    },

    getValidationItems: function () {
        var me = this;
        return [
            {
                fieldLabel: Uni.I18n.translate('reading.dataValidated.title', 'IMT', 'Data validated'),
                name: 'dataValidated',
                itemId: 'register-preview-dataValidated-field',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                fieldLabel: Uni.I18n.translate('reading.validationResult', 'IMT', 'Validation result'),
                name: 'validationResult',
                itemId: 'register-preview-validationResult-field',
                renderer: function (value) {
                    var record = me.down('form').getRecord();
                    return Imt.purpose.util.PreviewRenderer.renderValidationResult(value, record);
                }
            },
            {
                xtype: 'reading-qualities-field',
                router: me.router,
                itemId: 'register-preview-readingQualities-field',
                usedInInsight: true,
                name: 'validationRules',
                withOutAppName: me.withOutAppName
            }
        ];
    },

    getQualityItems: function () {
        return [
            {
                xtype: 'uni-form-info-message',
                itemId: 'register-preview-noReadings-msg',
                text: Uni.I18n.translate('general.noDataQualitiesMsg', 'IMT', 'There are no reading qualities for this data.'),
                padding: '10'
            },
            {
                fieldLabel: Uni.I18n.translate('general.deviceQuality', 'IMT', 'Device quality'),
                itemId: 'register-preview-deviceQuality-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.MDCQuality', 'IMT', 'MDC quality'),
                itemId: 'register-preview-multiSenseQuality-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.MDMQuality', 'IMT', 'MDM quality'),
                itemId: 'register-preview-insightQuality-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.thirdPartyQuality', 'IMT', 'Third party quality'),
                itemId: 'register-preview-thirdPartyQuality-field'
            }
        ];
    },

    updateForm: function (record, output) {
        var me = this,
            dataQualities = record.get('readingQualities'),
            title = me.getTitle(record),
            formula = me.getFormulaValue(output);

        Ext.suspendLayouts();
        me.down('#register-preview-general-panel').setTitle(title);
        me.down('#register-preview-validation-panel').setTitle(title);
        me.down('#register-preview-qualities-panel').setTitle(title);
        me.down('#register-preview-general-panel').loadRecord(record);
        me.down('#register-preview-validation-panel').loadRecord(record);
        me.down('#register-preview-formula-field').setValue(formula);
        me.down('#register-preview-noReadings-msg').setVisible(Ext.isEmpty(dataQualities));
        Imt.purpose.util.PreviewRenderer.renderDataQualityFields(
            me.down('#register-preview-deviceQuality-field'),
            me.down('#register-preview-multiSenseQuality-field'),
            me.down('#register-preview-insightQuality-field'),
            me.down('#register-preview-thirdPartyQuality-field'),
            dataQualities);
        Ext.resumeLayouts(true);
    },

    getTitle: function (record) {
        return Uni.DateTime.formatDateTimeShort(new Date(record.get('timeStamp')));
    },

    renderValueAndUnit: function (value, displayField) {
        if (value) {
            var record = displayField.up('form').getRecord();
            if (record) {
                return Uni.Number.formatNumber(value, -1) + ' ' + record.get('unitWithMultiplier');
            }
        }
        return '-'
    },

    getFormulaValue: function (output){
        return output.get('formula').description;
    }
});
