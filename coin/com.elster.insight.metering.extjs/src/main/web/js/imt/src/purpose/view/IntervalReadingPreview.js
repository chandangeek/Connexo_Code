/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Imt.purpose.view.IntervalReadingPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.interval-reading-preview',

    requires: [
        'Imt.purpose.util.DataFormatter',
        'Imt.purpose.util.PreviewRenderer',
        'Cfg.view.field.ReadingQualities'
    ],

    initComponent: function () {
        var me = this,
            generalItems = me.getGeneralItems(),
            valuesItems = me.getReadingValueItems(),
            qualityItems = me.getReadingQualityItems();

        me.items = [
            {
                title: Uni.I18n.translate('reading.generaltab.title', 'IMT', 'General'),
                items: {
                    xtype: 'form',
                    itemId: 'interval-preview-general-panel',
                    frame: true,
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: generalItems
                }
            },
            {
                title: Uni.I18n.translate('reading.readingvaluetab.title', 'IMT', 'Reading value'),
                items: {
                    xtype: 'form',
                    itemId: 'interval-preview-readingValue-panel',
                    frame: true,
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: valuesItems
                }
            },
            {
                title: Uni.I18n.translate('general.readingQuality', 'IMT', 'Reading quality'),
                items: {
                    xtype: 'form',
                    itemId: 'interval-preview-readingQuality-panel',
                    frame: true,
                    items: qualityItems,
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200,
                        htmlEncode: false
                    },
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
                fieldLabel: Uni.I18n.translate('general.interval', 'IMT', 'Interval'),
                name: 'interval',
                itemId: 'interval-preview-interval-field',
                renderer: Imt.purpose.util.DataFormatter.formatIntervalLong
            },
            {
                fieldLabel: Uni.I18n.translate('device.readingData.lastUpdate', 'IMT', 'Last update'),
                name: 'reportedDateTime',
                itemId: 'interval-preview-lastUpdate-field',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('reading.dataValidated.title', 'IMT', 'Data validated'),
                name: 'dataValidated',
                itemId: 'interval-preview-dataValidated-field',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                fieldLabel: Uni.I18n.translate('reading.validationResult', 'IMT', 'Validation result'),
                name: 'validationResult',
                itemId: 'interval-preview-validationResult-field',
                renderer: function (value) {
                    var record = me.down('form').getRecord();
                    return Imt.purpose.util.PreviewRenderer.renderValidationResult(value, record);
                }
            }
        ];
    },

    getReadingValueItems: function () {
        var me = this;
        return [
            {
                fieldLabel: Uni.I18n.translate('general.value', 'IMT', 'Value'),
                name: 'value',
                itemId: 'interval-preview-value-field',
                width: 400,
                renderer: function (value) {
                    var record = me.down('form').getRecord();
                    return Imt.purpose.util.PreviewRenderer.renderValueWithResult(value, record, me.output);
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.formula', 'IMT', 'Formula'),
                itemId: 'interval-preview-formula-field'
            },
            {
                xtype: 'reading-qualities-field',
                router: me.router,
                itemId: 'interval-preview-readingQualities-field',
                usedInInsight: true,
                name: 'validationRules',
                withOutAppName: me.withOutAppName
            },
            {
                itemId: 'estimation-comment-field',
                name: 'mainCommentValue',
                fieldLabel: Uni.I18n.translate('general.estimationComment', 'IMT', 'Estimation comment'),
                renderer: function (value) {
                    if (!value) {
                        this.hide();
                    } else {
                        this.show();
                        return value;
                    }
                }
            }

        ];
    },

    getReadingQualityItems: function () {
        return [
            {
                xtype: 'uni-form-info-message',
                itemId: 'interval-preview-noReadings-msg',
                text: Uni.I18n.translate('general.noDataQualitiesMsg', 'IMT', 'There are no reading qualities for this data.'),
                padding: '10'
            },
            {
                fieldLabel: Uni.I18n.translate('general.deviceQuality', 'IMT', 'Device quality'),
                itemId: 'interval-preview-deviceQuality-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.MDCQuality', 'IMT', 'MDC quality'),
                itemId: 'interval-preview-multiSenseQuality-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.MDMQuality', 'IMT', 'MDM quality'),
                itemId: 'interval-preview-insightQuality-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.thirdPartyQuality', 'IMT', 'Third party quality'),
                itemId: 'interval-preview-thirdPartyQuality-field'
            }
        ];
    },

    updateForm: function (record) {
        var me = this,
            dataQualities = record.get('readingQualities'),
            title = me.getTitle(record);

        Ext.suspendLayouts();
        me.down('#interval-preview-general-panel').setTitle(title);
        me.down('#interval-preview-readingValue-panel').setTitle(title);
        me.down('#interval-preview-readingQuality-panel').setTitle(title);
        me.down('#interval-preview-general-panel').loadRecord(record);
        me.down('#interval-preview-readingValue-panel').loadRecord(record);
        me.down('#interval-preview-formula-field').setValue(me.output.get('formula').description);
        me.down('#interval-preview-noReadings-msg').setVisible(Ext.isEmpty(dataQualities));
        Imt.purpose.util.PreviewRenderer.renderDataQualityFields(
            me.down('#interval-preview-deviceQuality-field'),
            me.down('#interval-preview-multiSenseQuality-field'),
            me.down('#interval-preview-insightQuality-field'),
            me.down('#interval-preview-thirdPartyQuality-field'),
            dataQualities);
        Ext.resumeLayouts(true);
    },

    getTitle: function(record) {
        var intervalEnd = record.get('readingTime');
        return Uni.DateTime.formatDateTime(intervalEnd,'long','short')
    }

});