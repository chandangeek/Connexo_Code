Ext.define('Mdc.view.setup.devicechannels.DataPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.deviceLoadProfileChannelDataPreview',
    itemId: 'deviceLoadProfileChannelDataPreview',
    requires: [
        'Mdc.view.setup.devicechannels.DataActionMenu',
        'Mdc.view.setup.devicechannels.ValidationPreview',
        'Uni.form.field.EditedDisplay',
        'Uni.util.FormInfoMessage'
    ],
    channelRecord: null,
    channels: null,
    frame: false,

    updateForm: function (record) {
        var me = this,
            intervalEnd = record.get('interval_end'),
            title =  Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateLong(intervalEnd), Uni.DateTime.formatTimeShort(intervalEnd)],
                false),
            mainValidationInfo,
            bulkValidationInfo,
            dataQualities,
            dataQualitiesForChannels = false,
            router = me.router;

        me.setLoading();
        record.refresh(router.arguments.mRID, router.arguments.channelId, function(){
            Ext.suspendLayouts();
            me.down('#general-panel').setTitle(title);
            me.down('#values-panel').setTitle(title);
            me.down('#mdc-qualities-panel').setTitle(title);
            me.down('#general-panel').loadRecord(record);
            me.down('#values-panel').loadRecord(record);

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
                        channelBulkValueField = me.down('#channelBulkValue' + channel.id);
                    if (record.get('channelValidationData')[channel.id]) {
                        mainValidationInfo = record.get('channelValidationData')[channel.id].mainValidationInfo;
                        bulkValidationInfo = record.get('channelValidationData')[channel.id].bulkValidationInfo;
                        dataQualities = record.get('channelValidationData')[channel.id].readingQualities;
                        if (mainValidationInfoField) {
                            me.setValidationInfo(mainValidationInfoField, mainValidationInfo);
                        }
                        if (bulkValidationInfo) {
                            me.setValidationInfo(me.down('#bulkValidationInfo' + channel.id), bulkValidationInfo);
                        }
                        if (me.down('#channelValue' + channel.id)) {
                            me.down('#channelValue' + channel.id).setValue(record.get('channelData')[channel.id]);
                        }
                        if (channelBulkValueField) {
                            channelBulkValueField.setValue(record.get('channelCollectedData')[channel.id]);
                        }
                        if (!Ext.isEmpty(dataQualities)) {
                            dataQualitiesForChannels |= true;
                            me.setDataQualityForChannel(channel.id, dataQualities);
                        }
                    } else {
                        if (mainValidationInfoField) {
                            mainValidationInfoField.hide();
                        }
                        me.down('#bulkValidationInfo' + channel.id).hide();
                    }
                    if (!dataQualitiesForChannels) {
                        me.down('#mdc-qualities-panel').removeAll();
                        me.down('#mdc-qualities-panel').add(
                            {
                                xtype: 'uni-form-info-message',
                                itemId: 'mdc-noReadings-msg',
                                text: Uni.I18n.translate('general.loadProfile.noDataQualities', 'MDC', 'There are no reading qualities for the channel readings on this load profile.'),
                                margin: '7 10 32 0',
                                padding: '10'
                            }
                        );
                    }
                });
                Ext.Array.findBy(me.channels, function (channel) {
                    if (record.get('channelValidationData')[channel.id]) {
                        me.down('#readingDataValidated').setValue(record.get('channelValidationData')[channel.id].dataValidated);
                        return !record.get('channelValidationData')[channel.id].dataValidated;
                    }
                });
            } else {
                var mainValidationInfoField = me.down('#mainValidationInfo');
                if (mainValidationInfoField) {
                    me.setValidationInfo(mainValidationInfoField, record.get('mainValidationInfo'));
                }
                me.setValidationInfo(me.down('#bulkValidationInfo'), record.get('bulkValidationInfo'));
                me.setDataQuality(record.get('readingQualities'));
                me.down('#readingDataValidated').setValue(record.get('dataValidated'));
            }

            Ext.resumeLayouts(true);
            me.setLoading(false);
        });
    },

    setValidationInfo: function (field, info) {
        var me = this,
            estimatedRule,
            estimatedRuleName,
            url;

        if (!field || !info) {
            return;
        }
        field.show();
        if (info.isConfirmed) {
            field.setValue(Uni.I18n.translate('general.confirmed', 'MDC', 'Confirmed'));
        } else if (!Ext.isEmpty(info.validationRules)) {
            me.setValidationRules(field, info.validationRules);
        } else if (info.estimatedByRule) {
            estimatedRule = info.estimatedByRule;
            url = me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl({ruleSetId: estimatedRule.ruleSetId, ruleId: estimatedRule.id});
            estimatedRuleName = estimatedRule.deleted ? estimatedRule.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') :
                '<a href="' + url + '">' + estimatedRule.name + '</a>';
            field.setValue(Uni.I18n.translate('deviceChannelData.estimatedAccordingTo', 'MDC', 'Estimated according to {0}',[estimatedRuleName], false));
        } else {
            field.hide();
        }
    },

    setValidationRules: function (field, value) {
        var str = '',
            prop,
            failEqualDataValue,
            intervalFlagsValue = '';

        Ext.Array.each(value, function (rule) {
            if (!Ext.isEmpty(rule.properties)) {
                switch (rule.implementation) {
                    case 'com.elster.jupiter.validators.impl.ThresholdValidator':
                        prop = ' - ' + rule.properties[0].key.charAt(0).toUpperCase() + rule.properties[0].key.substring(1) + ': ' + rule.properties[0].propertyValueInfo.value + ', ' +
                            rule.properties[1].key.charAt(0).toUpperCase() + rule.properties[1].key.substring(1) + ': ' + rule.properties[1].propertyValueInfo.value;
                        break;
                    case 'com.elster.jupiter.validators.impl.RegisterIncreaseValidator':
                        if (rule.properties[0].propertyValueInfo.value) {
                            failEqualDataValue = Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                        } else {
                            failEqualDataValue = Uni.I18n.translate('general.no', 'MDC', 'No');
                        }
                        prop = ' - ' + Uni.I18n.translate('device.registerData.failEqualData', 'MDC', 'Fail equal data') + ': ' + failEqualDataValue;
                        break;
                    case 'com.elster.jupiter.validators.impl.IntervalStateValidator':
                        Ext.Array.each(rule.properties[0].propertyValueInfo.value, function (idValue) {
                            Ext.Array.each(rule.properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues, function (item) {
                                if (idValue === item.id) {
                                    intervalFlagsValue += item.name + ', ';
                                }
                            });
                        });
                        intervalFlagsValue = intervalFlagsValue.slice(0, -2);
                        prop = ' - ' + Uni.I18n.translate('deviceloadprofiles.intervalFlags', 'MDC', 'Interval flags') + ': ' + intervalFlagsValue;
                        break;
                    default:
                        prop = '';
                        break;
                }
            } else {
                prop = '';
            }
            if (rule.deleted) {
                str += '<span style="word-wrap: break-word; display: inline-block; width: 800px">' + rule.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') + prop + '</span>' + '<br>';
            } else {
                str = '<span style="word-wrap: break-word; display: inline-block; width: 800px">';
                if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                    str += '<a href="#/administration/validation/rulesets/' + rule.ruleSetVersion.ruleSet.id + '/versions/' + rule.ruleSetVersion.id + '/rules/' + rule.id + '">' + rule.name + '</a>';
                } else {
                    str += rule.name;
                }
                str += prop + '</span>' + '<br>';
            }
        });
        field.setValue(str);
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
            me.down('#mdc-qualities-panel').remove(channelQualityContainer);
            return;
        }

        var deviceQualityField = me.down('#mdc-device-quality-' + channelId),
            multiSenseQualityField = me.down('#mdc-multiSense-quality-' + channelId),
            insightQualityField = me.down('#mdc-insight-quality-' + channelId),
            thirdPartyQualityField = me.down('#mdc-thirdParty-quality-' + channelId);

        me.setDataQualityFields(deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities);
    },

    setDataQualityFields: function(deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities) {
        var showDeviceQuality = false,
            showMultiSenseQuality = false,
            showInsightQuality = false,
            show3rdPartyQuality = false,
            field = undefined;

        deviceQualityField.setValue('');
        multiSenseQualityField.setValue('');
        insightQualityField.setValue('');
        thirdPartyQualityField.setValue('');

        Ext.Array.forEach(dataQualities, function(readingQuality) {
            if (readingQuality.cimCode.startsWith('1.')) {
                showDeviceQuality |= true;
                field = deviceQualityField;
            } else if (readingQuality.cimCode.startsWith('2.')) {
                showMultiSenseQuality |= true;
                field = multiSenseQualityField;
            } else if (readingQuality.cimCode.startsWith('3.')) {
                showInsightQuality |= true;
                field = insightQualityField;
            } else if (readingQuality.cimCode.startsWith('4.')||readingQuality.cimCode.startsWith('5.')) {
                show3rdPartyQuality |= true;
                field = thirdPartyQualityField;
            }
            if (!Ext.isEmpty(field)) {
                field.setValue(field.getValue()
                    + (Ext.isEmpty(field.getValue()) ? '' : '<br>')
                    + readingQuality.indexName + ' (' + readingQuality.cimCode + ')'
                );
            }
        });

        showDeviceQuality ? deviceQualityField.show() : deviceQualityField.hide();
        showMultiSenseQuality ? multiSenseQualityField.show() : multiSenseQualityField.hide();
        showInsightQuality ? insightQualityField.show() : insightQualityField.hide();
        show3rdPartyQuality ? thirdPartyQualityField.show() : thirdPartyQualityField.hide();
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
                        ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(value.start)),Uni.DateTime.formatTimeLong(new Date(value.start))])
                        + ' - ' +
                        Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(value.end)),Uni.DateTime.formatTimeLong(new Date(value.end))])
                        : '-';
                },
                htmlEncode: false
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                name: 'readingTime',
                renderer: function (value, field) {
                    return value ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(value)), Uni.DateTime.formatTimeLong(new Date(value))]) : '-';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.validationstatus.title', 'MDC', 'Validation status'),
                name: 'validationActive',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.active', 'MDC', 'Active') :
                        Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
                }
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.dataValidated.title', 'MDC', 'Data validated'),
                itemId: 'readingDataValidated',
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
                        fieldLabel: calculatedReadingType
                            ? Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated value')
                            : Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                        xtype: 'displayfield',
                        labelWidth: 200,
                        itemId: 'channelValue' + channel.id,
                        renderer: function (value) {
                            return me.setValueWithResult(value, 'main', channel);
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
                        xtype: 'displayfield',
                        labelWidth: 200,
                        itemId: (calculatedReadingType ? 'main' : 'bulk') + 'ValidationInfo' + channel.id,
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
                            fieldLabel: Uni.I18n.translate('general.readingQualities', 'MDC', 'Reading qualities'),
                            xtype: 'displayfield',
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
                        fieldLabel: Uni.I18n.translate('general.multiSenseQuality', 'MDC', 'MultiSense quality'),
                        itemId: 'mdc-multiSense-quality-' + channel.id,
                        labelWidth: 200,
                        htmlEncode: false
                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.insightQuality', 'MDC', 'Insight quality'),
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
            var calculatedReadingType = me.channelRecord.get('calculatedReadingType');
            valuesItems.push(
                {
                    xtype: 'fieldcontainer',
                    labelWidth: 200,
                    fieldLabel: calculatedReadingType
                        ? Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated value')
                        : Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
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
                    xtype: 'displayfield',
                    labelWidth: 200,
                    fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
                    itemId: calculatedReadingType ? 'mainValidationInfo' : 'bulkValidationInfo',
                    htmlEncode: false
                }
            );
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
                        xtype: 'displayfield',
                        labelWidth: 200,
                        fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
                        itemId: 'bulkValidationInfo',
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
                    margin: '7 10 32 0',
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
                    fieldLabel: Uni.I18n.translate('general.multiSenseQuality', 'MDC', 'MultiSense quality'),
                    itemId: 'mdc-multiSense-quality',
                    labelWidth: 200,
                    htmlEncode: false
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.insightQuality', 'MDC', 'Insight quality'),
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