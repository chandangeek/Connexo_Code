Ext.define('Mdc.view.setup.devicechannels.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelDataPreview',
    itemId: 'deviceLoadProfileChannelDataPreview',
    requires: [
        'Mdc.view.setup.devicechannels.DataActionMenu',
        'Uni.form.field.IntervalFlagsDisplay',
        'Mdc.view.setup.devicechannels.ValidationPreview',
        'Uni.form.field.EditedDisplay'
    ],
    channels: null,
    frame: false,

    updateForm: function (record) {
        var me = this,
            intervalEnd = record.get('interval_end'),
            title =  Uni.I18n.translate('general.dateattime', 'MDC', '{0} At {1}',[Uni.DateTime.formatDateLong(intervalEnd),Uni.DateTime.formatTimeLong(intervalEnd)]).toLowerCase(),
            mainValidationInfo,
            bulkValidationInfo;

        Ext.suspendLayouts();
        me.down('#general-panel').setTitle(title);
        me.down('#values-panel').setTitle(title);
        me.down('form').loadRecord(record);

        if (me.channels) {
            Ext.Array.each(me.channels, function (channel) {
                if (record.get('channelValidationData')[channel.id]) {
                    mainValidationInfo = record.get('channelValidationData')[channel.id].mainValidationInfo;
                    bulkValidationInfo = record.get('channelValidationData')[channel.id].bulkValidationInfo;
                    me.setReadingQualities(me.down('#mainReadingQualities' + channel.id), mainValidationInfo);
                    me.setReadingQualities(me.down('#bulkReadingQualities' + channel.id), bulkValidationInfo);
                    me.down('#channelValue' + channel.id).setValue(record.get('channelData')[channel.id]);
                    me.down('#channelBulkValue' + channel.id).setValue(record.get('channelCollectedData')[channel.id]);
                } else {
                    me.down('#mainReadingQualities' + channel.id).hide();
                    me.down('#bulkReadingQualities' + channel.id).hide();
                }
            });
            Ext.Array.findBy(me.channels, function (channel) {
                if (record.get('channelValidationData')[channel.id]) {
                    me.down('#readingDataValidated').setValue(record.get('channelValidationData')[channel.id].dataValidated);
                    return !record.get('channelValidationData')[channel.id].dataValidated;
                }
            });
            me.setGeneralReadingQualities(me.down('#generalReadingQualities'), me.up('deviceLoadProfilesData').loadProfile.get('validationInfo'));
        } else {
            mainValidationInfo = record.get('validationInfo').mainValidationInfo;
            bulkValidationInfo = record.get('validationInfo').bulkValidationInfo;
            me.setReadingQualities(me.down('#mainReadingQualities'), mainValidationInfo);
            me.setReadingQualities(me.down('#bulkReadingQualities'), bulkValidationInfo);
            me.setGeneralReadingQualities(me.down('#generalReadingQualities'), me.up('tabbedDeviceChannelsView').channel.get('validationInfo'));
            me.down('#readingDataValidated').setValue(record.get('validationInfo').dataValidated);
        }
        Ext.resumeLayouts(true);
    },

    setReadingQualities: function (field, info) {
        var me = this,
            estimatedRule,
            estimatedRuleName,
            url,
            view = me.up('tabbedDeviceChannelsView') || me.up('deviceLoadProfilesData');

        field.show();
        if (info.isConfirmed) {
            field.setValue(Uni.I18n.translate('general.confirmed', 'MDC', 'Confirmed'));
        } else if (!Ext.isEmpty(info.validationRules)) {
            me.setValidationRules(field, info.validationRules);
        } else if (info.estimatedByRule) {
            estimatedRule = info.estimatedByRule;
            url = view.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl({ruleSetId: estimatedRule.ruleSetId, ruleId: estimatedRule.id});
            estimatedRuleName = estimatedRule.deleted ? estimatedRule.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') :
                '<a href="' + url + '">' + estimatedRule.name + '</a>';
            field.setValue(Uni.I18n.translate('deviceChannelData.estimatedAccordingTo', 'MDC', 'Estimated according to {0}',[estimatedRuleName]));
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
                    str += '<a href="#/administration/validation/rulesets/' + rule.ruleSetVersion.id + '/rules/' + rule.id + '">' + rule.name + '</a>';
                } else {
                    str += rule.name;
                }
                str += prop + '</span>' + '<br>';
            }
        });
        field.setValue(str);
    },

    setGeneralReadingQualities: function (field, value) {
        var result = '',
            me = this,
            url,
            view = me.up('tabbedDeviceChannelsView') || me.up('deviceLoadProfilesData');

        if (!Ext.isEmpty(value.suspectReason)) {
            field.show();
            Ext.Array.each(value.suspectReason, function (rule) {
                if (rule.key.deleted) {
                    result += Ext.String.htmlEncode(rule.key.name) + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') + ' - ' + rule.value + ' ' + Uni.I18n.translate('general.suspects', 'MDC', 'suspects') + '<br>';
                } else {
                    if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                        url = view.router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({ruleSetId: rule.key.ruleSetVersion.ruleSet.id, versionId: rule.key.ruleSetVersion.id, ruleId: rule.key.id});
                        result += '<a href="' + url + '"> ' + Ext.String.htmlEncode(rule.key.name) + '</a>';
                    } else {
                        result = Ext.String.htmlEncode(rule.key.name);
                    }
                }   result += ' - ' + Uni.I18n.translate('general.xsuspects', 'MDC', '{0} suspects',[rule.value]) + '<br>';
            });
            field.setValue(result);
        } else {
            field.hide();
        }
    },

    setValueWithResult: function (value, type, channelId) {
        var me = this,
            record = me.down('form').getRecord(),
            measurementType,
            validationInfo,
            validationResultText = '',
            formatValue,
            channel;

        if (me.channels) {
            channel = Ext.Array.findBy(me.channels, function (item) {
                return item.id == channelId;
            });
            measurementType = channel.calculatedReadingType ? channel.calculatedReadingType.unit : channel.readingType.unit;
            if (record.get('channelValidationData')[channelId]) {
                validationInfo = (type == 'main')
                    ? record.get('channelValidationData')[channelId].mainValidationInfo
                    : record.get('channelValidationData')[channelId].bulkValidationInfo;
            }
        } else {
            measurementType = me.channelRecord.get('unitOfMeasure');
            validationInfo = record.get('validationInfo') ? record.get('validationInfo')[type + 'ValidationInfo'] : null;
        }

        if (validationInfo && validationInfo.validationResult) {
            switch (validationInfo.validationResult.split('.')[1]) {
                case 'notValidated':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated') + ')' +
                        '<img style="vertical-align: top; margin-left: 5px" width="16" height="16" src="../sky/build/resources/images/shared/Not-validated.png"/>';
                    break;
                case 'suspect':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.suspect', 'MDC', 'Suspect') + ')' +
                        '<img style="vertical-align: top; margin-left: 5px" width="16" height="16" src="../sky/build/resources/images/shared/Suspect.png"/>';
                    break;
                case 'ok':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notsuspect', 'MDC', 'Not suspect') + ')';
                    if (!me.channels && validationInfo.isConfirmed) {
                        validationResultText += '<span style="margin-left: 5px; vertical-align: top" class="icon-checkmark3"</span>';
                    }
                    break;
            }
        }

        if (!Ext.isEmpty(value)) {
            if (me.channels) {
                return value + ' ' + measurementType + ' ' + validationResultText;
            } else {
                formatValue = Uni.Number.formatNumber(value, -1);
                return !Ext.isEmpty(formatValue) ? formatValue + ' ' + measurementType + ' ' + validationResultText : '';
            }
        } else {
            return Uni.I18n.translate('general.missingx', 'MDC', 'Missing {0}',[validationResultText]);
        }
    },

    initComponent: function () {
        var me = this,
            generalItems = [],
            valuesItems = [];

        generalItems.push(
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                name: 'interval_formatted'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                name: 'readingTime_formatted'
            },
            {
                xtype: 'interval-flags-displayfield',
                name: 'intervalFlags',
                renderer: function (value, field) {
                    if (Ext.isEmpty(value)) {
                        field.hide();
                    } else {
                        return value;
                    }
                }
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.validationstatus.title', 'MDC', 'Validation status'),
                name: 'validationStatus',
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
                    return value ? Uni.I18n.translate('validationResults.dataValidatedYes', 'MDC', 'Yes') :
                        Uni.I18n.translate('validationResults.dataValidatedNo', 'MDC', 'No')
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.readingQualities', 'MDC', 'Reading qualities'),
                itemId: 'generalReadingQualities',
                htmlEncode: false
            }
        );

        if (me.channels) {
            Ext.Array.each(me.channels, function (channel) {
                valuesItems.push({
                    xtype: 'fieldcontainer',
                    fieldLabel: channel.name,
                    itemId: 'channelFieldContainer' + channel.id,
                    labelAlign: 'top',
                    labelWidth: 400,
                    layout: 'vbox',
                    margin: '20 0 0 0',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                            itemId: 'channelValue' + channel.id,
                            renderer: function (value) {
                                return me.setValueWithResult(value, 'main', channel.id);
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
                            itemId: 'mainReadingQualities' + channel.id,
                            htmlEncode: false
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.bulkValue', 'MDC', 'Bulk value'),
                            itemId: 'channelBulkValue' + channel.id,
                            renderer: function (value) {
                                return me.setValueWithResult(value, 'bulk', channel.id);
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.readingQualities', 'MDC', 'Reading qualities'),
                            itemId: 'bulkReadingQualities' + channel.id,
                            htmlEncode: false
                        }
                    ]
                });
            });
        } else {
            valuesItems.push(
                {
                    xtype: 'fieldcontainer',
                    labelWidth: 200,
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
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
                    itemId: 'mainReadingQualities',
                    htmlEncode: false
                },
                {
                    xtype: 'fieldcontainer',
                    labelWidth: 200,
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.bulkValue', 'MDC', 'Bulk value'),
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
                    itemId: 'bulkReadingQualities',
                    htmlEncode: false
                }
            );
        }

        me.items = {
            xtype: 'form',
            items: {
                xtype: 'tabpanel',
                items: [
                    {
                        title: Uni.I18n.translate('devicechannelsdata.generaltab.title', 'MDC', 'General'),
                        items: {
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
                            itemId: 'values-panel',
                            frame: true,
                            items: valuesItems,
                            layout: 'vbox'
                        }
                    }
                ]
            }
        };
        me.callParent(arguments);
    }
});