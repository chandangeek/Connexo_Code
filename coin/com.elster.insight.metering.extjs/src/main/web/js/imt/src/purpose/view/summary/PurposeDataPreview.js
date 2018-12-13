/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.PurposeDataPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.purpose-data-preview',

    requires: [
        'Imt.purpose.util.DataFormatter',
        'Imt.purpose.util.PreviewRenderer',
        'Cfg.view.field.ReadingQualities'
    ],

    outputs: null,

    initComponent: function () {
        var me = this;

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
                    items: me.getGeneralItems()
                }
            },
            {
                title: Uni.I18n.translate('reading.readingvaluetab.title', 'IMT', 'Reading value'),
                items: {
                    xtype: 'form',
                    itemId: 'interval-preview-readingValue-panel',
                    frame: true,
                    layout: 'vbox',
                    items: [ {
                        xtype: 'container',
                        itemId: 'reading-value-container'
                    }]
                }
            },
            {
                title: Uni.I18n.translate('general.readingQuality', 'IMT', 'Reading quality'),
                items: {
                    xtype: 'form',
                    itemId: 'interval-preview-readingQuality-panel',
                    frame: true,
                    layout: 'vbox',
                    items: [ {
                        xtype: 'container',
                        itemId: 'reading-quality-container'
                    }]

                }
            }
        ];
        me.callParent(arguments);
    },

    getGeneralItems: function(){
        return [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.interval', 'IMT', 'Interval'),
                itemId: 'interval-preview-interval-field',
                width: 800,
                renderer: Imt.purpose.util.DataFormatter.formatIntervalLong
            },
            {
                // Added it here for future implementations
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('device.readingData.lastUpdate', 'IMT', 'Last update'),
                itemId: 'interval-preview-lastUpdate-field',
                renderer: Imt.purpose.util.DataFormatter.formatDateLong
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('reading.dataValidated.title', 'IMT', 'Data validated'),
                itemId: 'interval-preview-dataValidated-field',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            }
        ]
    },

    updateForm: function (record) {
        var me = this,
            channels = record.get('channelData'),
            firstTimeAddContainer = me.down('#reading-value-container').items.getCount() === 0,
            itemId = 0; // Created for test automation

        Ext.suspendLayouts();

        me.setTitle(record);
        me.setGeneralItems(record);
        if (firstTimeAddContainer){
            for (var channelId in channels) {
                itemId++;
                me.addReadingValueItems(itemId, channelId, channels);
                me.addReadingQualityItems(itemId, channelId, channels);
            }
        } else {
            for (var channelId in channels) {
                itemId++;
                me.setReadingValueItems(itemId, channelId, channels);
                me.setReadingQualityItems(itemId, channelId, channels);
            }
        }

        Ext.resumeLayouts(true);
    },

    setGeneralItems: function(record){
        var me = this,
            interval = record.get('interval');
        me.down('#interval-preview-interval-field').setValue(interval);
        me.down('#interval-preview-dataValidated-field').setValue(me.isDataValidated(record));
    },

    isDataValidated: function(record){
        var isDataValidated = true,
            channels = record.get('channelData');
        for (var channelId in channels){
            if (channels[channelId].dataValidated === false){
                isDataValidated = false;
                break;
            }
        }
        return isDataValidated;
    },

    addReadingValueItems: function (id, channelId, channels) {
        var me = this,
            readingValuesContainer = me.down('#reading-value-container');

        readingValuesContainer.add({
            xtype: 'fieldcontainer',
            fieldLabel: me.getOutputName(channelId),
            itemId: 'interval-preview-name-output' + id,
            labelAlign: 'top',
            labelWidth: 400,
            layout: 'vbox',
            margin: '20 0 0 0',
            items: []
        });

        readingValuesContainer.add({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('general.value', 'IMT', 'Value'),
            itemId: 'interval-preview-value-field' + id,
            value: channels[channelId].value,
            renderer: function (value) {
                return value ? Uni.Number.formatNumber(value, -1) + " " + me.getOutputUnitOfMeasure(channelId) : '-'
            }
        });
        readingValuesContainer.add({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('general.formula', 'IMT', 'Formula'),
            itemId: 'interval-preview-formula-field' + id,
            value: me.getOutputFormula(channelId)
        });
        readingValuesContainer.add({
            xtype: 'reading-qualities-field',
            router: me.router,
            itemId: 'interval-preview-readingQualities-field' + id,
            usedInInsight: true,
            value: channels[channelId].validationRules
        });
    },

    setReadingValueItems: function (id, channelId, channels) {
        var me = this;
        me.down('#interval-preview-name-output' + id).setFieldLabel(me.getOutputName(channelId));
        me.down('#interval-preview-value-field' + id).setValue(channels[channelId].value);
        me.down('#interval-preview-formula-field' + id).setValue(me.getOutputFormula(channelId));
        me.down('#interval-preview-readingQualities-field' + id).setValue(channels[channelId].validationRules);
    },

    addReadingQualityItems: function(id, channelId, channels){
        var me = this,
            readingQualityContainer = me.down('#reading-quality-container');

        readingQualityContainer.add({
            xtype: 'fieldcontainer',
            fieldLabel: me.getOutputName(channelId),
            itemId: 'interval-preview-quality-output' + id,
            labelAlign: 'top',
            labelWidth: 400,
            layout: 'vbox',
            margin: '20 0 0 0',
            items: []
        });

        readingQualityContainer.add({
            xtype: 'uni-form-info-message',
            itemId: 'interval-preview-noReadings-msg',
            text: Uni.I18n.translate('general.noDataQualitiesMsg', 'IMT', 'There are no reading qualities for this data.'),
            padding: '10'
        });

        readingQualityContainer.add({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('general.deviceQuality', 'IMT', 'Device quality'),
            itemId: 'interval-preview-deviceQuality-field' + id,
            htmlEncode: false
        });

        readingQualityContainer.add({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('general.MDCQuality', 'IMT', 'MDC quality'),
            itemId: 'interval-preview-multiSenseQuality-field' + id,
            htmlEncode: false
        });

        readingQualityContainer.add({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('general.MDMQuality', 'IMT', 'MDM quality'),
            itemId: 'interval-preview-insightQuality-field' + id,
            htmlEncode: false
        });

        readingQualityContainer.add({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('general.thirdPartyQuality', 'IMT', 'Third party quality'),
            itemId: 'interval-preview-thirdPartyQuality-field' + id,
            htmlEncode: false
        });

        me.down('#interval-preview-noReadings-msg').setVisible(Ext.isEmpty(channels[channelId].readingQualities));
        Imt.purpose.util.PreviewRenderer.renderDataQualityFields(
            me.down('#interval-preview-deviceQuality-field' + id),
            me.down('#interval-preview-multiSenseQuality-field' + id),
            me.down('#interval-preview-insightQuality-field' + id),
            me.down('#interval-preview-thirdPartyQuality-field' + id),
            channels[channelId].readingQualities);
    },

    setReadingQualityItems: function(id, channelId, channels){
        var me = this;
        me.down('#interval-preview-noReadings-msg').setVisible(Ext.isEmpty(channels[channelId].readingQualities));
        Imt.purpose.util.PreviewRenderer.renderDataQualityFields(
            me.down('#interval-preview-deviceQuality-field' + id),
            me.down('#interval-preview-multiSenseQuality-field' + id),
            me.down('#interval-preview-insightQuality-field' + id),
            me.down('#interval-preview-thirdPartyQuality-field' + id),
            channels[channelId].readingQualities);
    },


    getOutputFormula: function(id){
        var output = this.outputs.findRecord('id', id);
        return output.get('formula').description;
    },

    getOutputName: function(id){
        var output = this.outputs.findRecord('id', id);
        return output.get('name');
    },

    getOutputUnitOfMeasure: function(id){
        var output = this.outputs.findRecord('id', id);
        return output.get('readingType').names.unitOfMeasure;
    },

    setTitle: function(record){
        var me = this,
            intervalEnd = record.get('interval').end,
            title = Uni.DateTime.formatDateTime(intervalEnd,'long','short');

        me.down('#interval-preview-general-panel').setTitle(title);
        me.down('#interval-preview-readingValue-panel').setTitle(title);
        me.down('#interval-preview-readingQuality-panel').setTitle(title);
    }
});