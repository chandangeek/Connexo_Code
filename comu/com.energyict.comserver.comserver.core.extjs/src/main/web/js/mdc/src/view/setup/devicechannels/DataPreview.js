/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.DataPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.deviceLoadProfileChannelDataPreview',
    itemId: 'deviceLoadProfileChannelDataPreview',
    requires: [
        'Mdc.view.setup.devicechannels.DataActionMenu',
        'Mdc.view.setup.devicechannels.ValidationPreview',
        'Uni.form.field.EditedDisplay',
        'Cfg.view.field.ReadingQualities',
        'Uni.util.FormInfoMessage'
    ],
    channelRecord: null,
    channels: null,
    router: null,
    frame: false,
    mentionDataLoggerSlave: false,

    updateForm: function(record) {
        var me = this,
            intervalEnd = record.get('interval_end'),
            title = Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateLong(intervalEnd), Uni.DateTime.formatTimeShort(intervalEnd)],
                false),
            mainValidationInfo,
            bulkValidationInfo,
            dataQualities,
            dataQualitiesForChannels = false,            
            router = me.router;

        me.setLoading();
        record.getDetailedInformation(router.arguments.deviceId, router.arguments.channelId, function (detailRecord) {
            Ext.suspendLayouts();
            me.down('#general-panel').setTitle(title);
            me.down('#values-panel').setTitle(title);
            me.down('#mdc-qualities-panel').setTitle(title);
            me.down('#general-panel').loadRecord(record);

            if (record.get('multiplier')) {
                me.down('#general-panel #mdc-multiplier').show();
                me.down('#values-panel #mdc-multiplier').show();
            } else {
                me.down('#general-panel #mdc-multiplier').hide();
                me.down('#values-panel #mdc-multiplier').hide();
            }

            if (me.channels) {
                dataQualitiesForChannels = false;
                Ext.Array.each(me.channels, function (channel) {
                    var mainValidationInfoField = me.down('#mainValidationInfo' + channel.id),
                        channelBulkValueField = me.down('#channelBulkValue' + channel.id),
                        containter = me.down("#channelFieldContainer" + channel.id);

                    if (detailRecord.get('channelValidationData')[channel.id]) {
                        mainValidationInfo = detailRecord.get('channelValidationData')[channel.id].mainValidationInfo;
                        bulkValidationInfo = detailRecord.get('channelValidationData')[channel.id].bulkValidationInfo;
                        dataQualities = detailRecord.get('channelValidationData')[channel.id].readingQualities;
                        containter.down('#mainValidationInfo' + channel.id).setValue(mainValidationInfo);
                        if (containter.down('#bulkValidationInfo' + channel.id)) {
                            containter.down('#bulkValidationInfo' + channel.id).setValue(bulkValidationInfo);
                        }
                        if (me.down('#channelValue' + channel.id)) {
                            me.down('#channelValue' + channel.id).setValue(detailRecord.get('channelData')[channel.id]);
                        }
                        if (channelBulkValueField) {
                            channelBulkValueField.setValue(detailRecord.get('channelCollectedData')[channel.id]);
                        }
                        dataQualitiesForChannels |= !Ext.isEmpty(dataQualities);
                        me.setDataQualityForChannel(channel.id, dataQualities);
                    } else {
                        if (mainValidationInfoField) {
                            mainValidationInfoField.hide();
                        }
                        me.down('#bulkValidationInfo' + channel.id).hide();
                    }
                });
                if (!dataQualitiesForChannels) {
                    me.down('#mdc-noReadings-msg').show();
                } else {
                    me.down('#mdc-noReadings-msg').hide();
                }
                Ext.Array.findBy(me.channels, function (channel) {
                    if (detailRecord.get('channelValidationData')[channel.id]) {
                        me.down('#readingDataValidated').setValue(detailRecord.get('channelValidationData')[channel.id].dataValidated);
                        return !detailRecord.get('channelValidationData')[channel.id].dataValidated;
                    }
                });
            } else {
                me.setDataQuality(detailRecord.get('readingQualities'));
                me.down('#readingDataValidated').setValue(detailRecord.get('dataValidated'));
                var dataLoggerSlaveField = me.down('#mdc-channel-data-preview-data-logger-slave');
                if (dataLoggerSlaveField) {
                    dataLoggerSlaveField.setValue(record.get('slaveChannel'));
                }
            }

            Ext.resumeLayouts(true);
            detailRecord.set('value', record.get('value'));
            detailRecord.set('collectedValue', record.get('collectedValue'));
            detailRecord.set('multiplier', record.get('multiplier'));
            me.down('#values-panel').loadRecord(detailRecord);
            me.setLoading(false);
        });
    },

    setValueWithResult: function (value, type, channel) {
        var me = this,
            record = me.down('form').getRecord(),
            unitOfMeasure,
            validationInfo,
            validationResultText = '',
            formatValue,
            channelId = channel ? channel.id : undefined;

        if (type === 'main') {
            unitOfMeasure = me.channelRecord
                ? (me.channelRecord.get('calculatedReadingType')
                    ? me.channelRecord.get('calculatedReadingType').names.unitOfMeasure
                    : me.channelRecord.get('readingType').names.unitOfMeasure)
                : (channel.calculatedReadingType
                    ? channel.calculatedReadingType.names.unitOfMeasure
                    : channel.readingType.names.unitOfMeasure);
        } else { // 'bulk'
            unitOfMeasure = me.channelRecord
                ? me.channelRecord.get('readingType').names.unitOfMeasure
                : channel.readingType.names.unitOfMeasure;
        }
        if (me.channels) {
            if (record && record.get('channelValidationData')[channelId]) {
                validationInfo = (type == 'main')
                    ? record.get('channelValidationData')[channelId].mainValidationInfo
                    : record.get('channelValidationData')[channelId].bulkValidationInfo;
            }
        } else {
            if (record) {
                validationInfo = record.get(type + 'ValidationInfo');
            }
        }

        if (validationInfo && validationInfo.validationResult) {
            switch (validationInfo.validationResult.split('.')[1]) {
                case 'notValidated':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated') + ')' +
                        '<span class="icon-flag6" style="margin-left:10px; display:inline-block; vertical-align:top;"></span>';
                    break;
                case 'suspect':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.suspect', 'MDC', 'Suspect') + ')' +
                        '<span class="icon-flag5" style="margin-left:10px; display:inline-block; vertical-align:top; color:red;"></span>';
                    break;
                case 'ok':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notsuspect', 'MDC', 'Not suspect') + ')';
                    if (!me.channels && validationInfo.isConfirmed) {
                        validationResultText += '<span class="icon-checkmark" style="margin-left:5px; vertical-align:top;"></span>';
                    }
                    break;
            }
        }

        if (!Ext.isEmpty(value)) {
            formatValue = Uni.Number.formatNumber(
                value.toString(),
                me.channelRecord && !Ext.isEmpty(me.channelRecord.get('overruledNbrOfFractionDigits')) ? me.channelRecord.get('overruledNbrOfFractionDigits') : -1
            );
            return !Ext.isEmpty(formatValue) ? formatValue + ' ' + unitOfMeasure + ' ' + validationResultText : '';
        } else {
            if(type === 'main'){
                return Uni.I18n.translate('general.missingx', 'MDC', 'Missing {0}',[validationResultText], false);
            } else {
                return Uni.I18n.translate('general.missingx', 'MDC', 'Missing {0}',[validationResultText], false);
            }
        }
    },

    setDataQuality: function(dataQualities) {
        var me = this,
            deviceQualityField = me.down('#mdc-device-quality'),
            multiSenseQualityField = me.down('#mdc-multiSense-quality'),
            insightQualityField = me.down('#mdc-insight-quality'),
            thirdPartyQualityField = me.down('#mdc-thirdParty-quality');

        if (Ext.isEmpty(dataQualities)) {
            me.down('#mdc-noReadings-msg').show();
        } else {
            me.down('#mdc-noReadings-msg').hide();
        }
        me.setDataQualityFields(deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities);
    },

    setDataQualityForChannel: function(channelId, dataQualities) {
        var me = this,
            channelQualityContainer = me.down('#channelQualityContainer' + channelId);

        if (Ext.isEmpty(dataQualities)) {
            channelQualityContainer.hide();
            return;
        } else {
            channelQualityContainer.show();
        }

        var deviceQualityField = me.down('#mdc-device-quality-' + channelId),
            multiSenseQualityField = me.down('#mdc-multiSense-quality-' + channelId),
            insightQualityField = me.down('#mdc-insight-quality-' + channelId),
            thirdPartyQualityField = me.down('#mdc-thirdParty-quality-' + channelId);

        me.setDataQualityFields(deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities);
    },

    setDataQualityFields: function(deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities) {
        var me = this,
            showDeviceQuality = false,
            showMultiSenseQuality = false,
            showInsightQuality = false,
            show3rdPartyQuality = false,
            field = undefined;

        deviceQualityField.setValue('');
        multiSenseQualityField.setValue('');
        insightQualityField.setValue('');
        thirdPartyQualityField.setValue('');

        if (!Ext.isEmpty(dataQualities)) {
            Ext.Array.forEach(dataQualities, function (readingQuality) {
                if (Ext.String.startsWith(readingQuality.cimCode, '1.')) {
                    showDeviceQuality |= true;
                    field = deviceQualityField;
                } else if (Ext.String.startsWith(readingQuality.cimCode, '2.')) {
                    showMultiSenseQuality |= true;
                    field = multiSenseQualityField;
                } else if (Ext.String.startsWith(readingQuality.cimCode, '3.')) {
                    showInsightQuality |= true;
                    field = insightQualityField;
                } else if (Ext.String.startsWith(readingQuality.cimCode, '4.') || Ext.String.startsWith(readingQuality.cimCode, '5.')) {
                    show3rdPartyQuality |= true;
                    field = thirdPartyQualityField;
                }
                if (!Ext.isEmpty(field)) {
                    field.setValue(field.getValue()
                        + (Ext.isEmpty(field.getValue()) ? '' : '<br>')
                        + '<span style="display:inline-block; float: left; margin-right:7px;" >' + readingQuality.indexName + ' (' + readingQuality.cimCode + ')' + '</span>'
                        + '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="'
                        + me.getTooltip(readingQuality.systemName, readingQuality.categoryName, readingQuality.indexName) + '"></span>'
                    );
                }
            });
        }

        showDeviceQuality ? deviceQualityField.show() : deviceQualityField.hide();
        showMultiSenseQuality ? multiSenseQualityField.show() : multiSenseQualityField.hide();
        showInsightQuality ? insightQualityField.show() : insightQualityField.hide();
        show3rdPartyQuality ? thirdPartyQualityField.show() : thirdPartyQualityField.hide();
    },

    getTooltip: function(systemName, categoryName, indexName) {
        var me = this,
            tooltip = '<table><tr><td>';

        tooltip += '<b>' + Uni.I18n.translate('general.readingQuality.field1.name', 'MDC', 'System') + ':</b></td>';
        tooltip += '<td>' + systemName + '</td></tr>';
        tooltip += '<tr><td><b>' + Uni.I18n.translate('general.readingQuality.field2.name', 'MDC', 'Category') + ':</b></td>';
        tooltip += '<td>' + categoryName + '</td></tr>';
        tooltip += '<tr><td><b>' + Uni.I18n.translate('general.readingQuality.field3.name', 'MDC', 'Index') + ':</b></td>';
        tooltip += '<td>' + indexName + '</td></tr></table>';
        return tooltip;
    },

    initComponent: function () {
        var me = this,
            generalItems = [],
            valuesItems = [],
            qualityItems = [];

        generalItems.push(
            {
                fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                name: 'interval',
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                            [Uni.DateTime.formatDateLong(new Date(value.start)),Uni.DateTime.formatTimeLong(new Date(value.start))])
                          + ' - ' +
                          Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                            [Uni.DateTime.formatDateLong(new Date(value.end)),Uni.DateTime.formatTimeLong(new Date(value.end))])
                        : '';
                },
                htmlEncode: false
            }
        );

        if (me.mentionDataLoggerSlave) {
            generalItems.push(
                {
                    fieldLabel: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
                    itemId: 'mdc-channel-data-preview-data-logger-slave',
                    renderer: function(slaveChannel) {
                        if (Ext.isEmpty(slaveChannel)) {
                            return '-';
                        }
                        var slaveId = slaveChannel.deviceName,
                            channelId = slaveChannel.channelId;
                        return Ext.String.format('<a href="{0}">{1}</a>',
                            me.router.getRoute('devices/device/channels/channeldata').buildUrl(
                                {
                                    deviceId: encodeURIComponent(slaveId),
                                    channelId: channelId
                                },
                                me.router.queryParams
                            ),
                            slaveId);
                    }
                }
            );
        }

        generalItems.push(
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                name: 'readingTime',
                renderer: function (value, field) {
                    return value ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateLong(new Date(value)), Uni.DateTime.formatTimeLong(new Date(value))]) : '-';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.dataValidated.title', 'MDC', 'Data validated'),
                itemId: 'readingDataValidated',
                name: 'dataValidated',
                htmlEncode: false,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                name: 'multiplier',
                itemId: 'mdc-multiplier',
                hidden: true
            }
        );

        if (me.channels) {
            qualityItems.push(
                {
                    xtype: 'uni-form-info-message',
                    itemId: 'mdc-noReadings-msg',
                    text: Uni.I18n.translate('general.loadProfile.noDataQualities', 'MDC', 'There are no reading qualities for the channel readings on this load profile.'),
                    padding: '10'
                }
            );

            Ext.Array.each(me.channels, function (channel) {
                var calculatedReadingType = channel.calculatedReadingType,
                    channelName = !Ext.isEmpty(channel.calculatedReadingType)
                        ? channel.calculatedReadingType.fullAliasName
                        : channel.readingType.fullAliasName,
                    valueItem = {
                        xtype: 'fieldcontainer',
                        fieldLabel: channelName,
                        itemId: 'channelFieldContainer' + channel.id,
                        labelAlign: 'top',
                        labelWidth: 400,
                        layout: 'vbox',
                        margin: '20 0 0 0',
                        items: []
                    },
                    qualityItem = {
                        xtype: 'fieldcontainer',
                        fieldLabel: channelName,
                        itemId: 'channelQualityContainer' + channel.id,
                        labelAlign: 'top',
                        labelWidth: 400,
                        layout: 'vbox',
                        margin: '20 0 0 0',
                        items: []
                    };

                valueItem.items.push(
                    {
                        fieldLabel: Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated value'),
                        xtype: 'displayfield',
                        labelWidth: 200,
                        itemId: 'channelValue' + channel.id,
                        renderer: function (value) {
                            return me.setValueWithResult(value, 'main', channel);
                        }
                    },
                    {
                        xtype: 'reading-qualities-field',
                        router: me.router,
                        labelWidth: 200,
                        itemId: 'mainValidationInfo' + channel.id,
                        htmlEncode: false
                    }
                );
                if (calculatedReadingType) {
                    valueItem.items.push(
                        {
                            fieldLabel: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                            xtype: 'displayfield',
                            labelWidth: 200,
                            itemId: 'channelBulkValue' + channel.id,
                            renderer: function (value) {
                                return me.setValueWithResult(value, 'bulk', channel);
                            }
                        },
                        {
                            xtype: 'reading-qualities-field',
                            router: me.router,
                            labelWidth: 200,
                            itemId: 'bulkValidationInfo' + channel.id,
                            htmlEncode: false
                        }
                    );
                }
                valueItem.items.push(
                    {
                        fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                        xtype: 'displayfield',
                        labelWidth: 200,
                        name: 'multiplier',
                        itemId: 'mdc-multiplier',
                        hidden: true
                    }
                );
                valuesItems.push(valueItem);


                qualityItem.items.push(
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.deviceQuality', 'MDC', 'Device quality'),
                        itemId: 'mdc-device-quality-' + channel.id,
                        labelWidth: 200,
                        htmlEncode: false
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.MDCQuality', 'MDC', 'MDC quality'),
                        itemId: 'mdc-multiSense-quality-' + channel.id,
                        labelWidth: 200,
                        htmlEncode: false
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.MDMQuality', 'MDC', 'MDM quality'),
                        itemId: 'mdc-insight-quality-' + channel.id,
                        labelWidth: 200,
                        htmlEncode: false
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.thirdPartyQuality', 'MDC', 'Third party quality'),
                        itemId: 'mdc-thirdParty-quality-' + channel.id,
                        labelWidth: 200,
                        htmlEncode: false
                    }
                );
                qualityItems.push(qualityItem);
            });
        } else {
            valuesItems.push(
                {
                    xtype: 'fieldcontainer',
                    labelWidth: 200,
                    fieldLabel: Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated value'),
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'displayfield',
                            name: 'value',
                            renderer: function (value) {
                                return me.setValueWithResult(value, 'main');
                            }
                        },
                        {
                            xtype: 'edited-displayfield',
                            name: 'mainModificationState',
                            margin: '0 0 0 10'
                        }
                    ]
                },
                {
                    xtype: 'reading-qualities-field',
                    router: me.router,
                    labelWidth: 200,
                    itemId: 'mainValidationInfo',
                    name: 'mainValidationInfo',
                    htmlEncode: false
                }
            );
            var calculatedReadingType = me.channelRecord.get('calculatedReadingType');
            if (calculatedReadingType) {
                valuesItems.push(
                    {
                        xtype: 'fieldcontainer',
                        labelWidth: 200,
                        fieldLabel: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'collectedValue',
                                renderer: function (value) {
                                    return me.setValueWithResult(value, 'bulk');
                                }
                            },
                            {
                                xtype: 'edited-displayfield',
                                name: 'bulkModificationState',
                                margin: '0 0 0 10'
                            }
                        ]
                    },
                    {
                        xtype: 'reading-qualities-field',
                        router: me.router,
                        labelWidth: 200,
                        itemId: 'bulkValidationInfo',
                        name: 'bulkValidationInfo',
                        htmlEncode: false
                    }
                );
            }

            valuesItems.push(
                {
                    xtype: 'displayfield',
                    labelWidth: 200,
                    fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                    name: 'multiplier',
                    itemId: 'mdc-multiplier',
                    hidden: true
                }
            );

            qualityItems.push(
                {
                    xtype: 'uni-form-info-message',
                    itemId: 'mdc-noReadings-msg',
                    text: Uni.I18n.translate('general.reading.noDataQualities', 'MDC', 'There are no reading qualities for this reading.'),
                    padding: '10'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.deviceQuality', 'MDC', 'Device quality'),
                    itemId: 'mdc-device-quality',
                    labelWidth: 200,
                    htmlEncode: false
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.MDCQuality', 'MDC', 'MDC quality'),
                    itemId: 'mdc-multiSense-quality',
                    labelWidth: 200,
                    htmlEncode: false
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.MDMQuality', 'MDC', 'MDM quality'),
                    itemId: 'mdc-insight-quality',
                    labelWidth: 200,
                    htmlEncode: false
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.thirdPartyQuality', 'MDC', 'Third party quality'),
                    itemId: 'mdc-thirdParty-quality',
                    labelWidth: 200,
                    htmlEncode: false
                }
            );
        }

        me.items = [
            {
                title: Uni.I18n.translate('devicechannelsdata.generaltab.title', 'MDC', 'General'),
                items: {
                    xtype: 'form',
                    itemId: 'general-panel',
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
                title: Uni.I18n.translate('devicechannelsdata.readingvaluetab.title', 'MDC', 'Reading values'),
                items: {
                    xtype: 'form',
                    itemId: 'values-panel',
                    frame: true,
                    items: valuesItems,
                    layout: 'vbox'
                }
            },
            {
                title: Uni.I18n.translate('devicechannelsdata.readingqualitytab.title', 'MDC', 'Reading quality'),
                items: {
                    xtype: 'form',
                    itemId: 'mdc-qualities-panel',
                    frame: true,
                    items: qualityItems,
                    layout: 'vbox'
                }
            }
        ];
        me.callParent(arguments);
    }
});