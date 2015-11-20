Ext.define('Imt.channeldata.view.DataPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.channelDataPreview',
    itemId: 'channelDataPreview',
    requires: [
        'Uni.form.field.IntervalFlagsDisplay'
    ],
    frame: false,

    updateForm: function (record) {
        var me = this,
            intervalEnd = record.get('interval_end'),
            title =  Uni.I18n.translate('general.dateattime', 'IMT', '{0} At {1}',[Uni.DateTime.formatDateLong(intervalEnd),Uni.DateTime.formatTimeLong(intervalEnd)], false).toLowerCase(),
            mainValidationInfo,
            router = me.router
        ;
        
        Ext.suspendLayouts();
        me.down('#general-panel').setTitle(title);
        me.down('#values-panel').setTitle(title);
        me.down('#general-panel').loadRecord(record);
        me.down('#values-panel').loadRecord(record);

        me.setReadingQualities(me.down('#mainReadingQualities'), record.get('mainValidationInfo'));
        me.setGeneralReadingQualities(me.down('#generalReadingQualities'), record.get('readingQualities'));
        me.down('#readingDataValidated').setValue(record.get('dataValidated'));

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
            field.setValue(Uni.I18n.translate('general.confirmed', 'IMT', 'Confirmed'));
        } else if (!Ext.isEmpty(info.validationRules)) {
            me.setValidationRules(field, info.validationRules);
        } else if (info.estimatedByRule) {
            estimatedRule = info.estimatedByRule;
            url = me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl({ruleSetId: estimatedRule.ruleSetId, ruleId: estimatedRule.id});
            estimatedRuleName = estimatedRule.deleted ? estimatedRule.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'IMT', '(removed rule)') :
                '<a href="' + url + '">' + estimatedRule.name + '</a>';
            field.setValue(Uni.I18n.translate('deviceChannelData.estimatedAccordingTo', 'IMT', 'Estimated according to {0}',[estimatedRuleName], false));
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
                            failEqualDataValue = Uni.I18n.translate('general.yes', 'IMT', 'Yes');
                        } else {
                            failEqualDataValue = Uni.I18n.translate('general.no', 'IMT', 'No');
                        }
                        prop = ' - ' + Uni.I18n.translate('device.registerData.failEqualData', 'IMT', 'Fail equal data') + ': ' + failEqualDataValue;
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
                        prop = ' - ' + Uni.I18n.translate('deviceloadprofiles.intervalFlags', 'IMT', 'Interval flags') + ': ' + intervalFlagsValue;
                        break;
                    default:
                        prop = '';
                        break;
                }
            } else {
                prop = '';
            }
            if (rule.deleted) {
                str += '<span style="word-wrap: break-word; display: inline-block; width: 800px">' + rule.name + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'IMT', '(removed rule)') + prop + '</span>' + '<br>';
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

    setGeneralReadingQualities: function (field, value) {
        var result = '',
            me = this,
            url;

        if (value && !Ext.isEmpty(value.suspectReason)) {
            field.show();
            Ext.Array.each(value.suspectReason, function (rule) {
                if (rule.key.deleted) {
                    result += Ext.String.htmlEncode(rule.key.name) + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'IMT', '(removed rule)') + ' - ' + rule.value + ' ' + Uni.I18n.translate('general.suspects', 'IMT', 'suspects') + '<br>';
                } else {
                    if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                        url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({ruleSetId: rule.key.ruleSetVersion.ruleSet.id, versionId: rule.key.ruleSetVersion.id, ruleId: rule.key.id});
                        result += '<a href="' + url + '"> ' + Ext.String.htmlEncode(rule.key.name) + '</a>';
                    } else {
                        result = Ext.String.htmlEncode(rule.key.name);
                    }
                }
                result += ' - ' + Uni.I18n.translate('general.xsuspects', 'IMT', '{0} suspects',[rule.value]) + '<br>';
            });
            field.setValue(result);
        } else if (!Ext.isEmpty(value)) {
            field.show();
            Ext.Array.each(value, function (rule) {
                result += Ext.String.htmlEncode(rule.name) + '<br>';
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

        measurementType = me.channelRecord.get('unitOfMeasure');
        validationInfo = record.get(type + 'ValidationInfo');

        if (validationInfo && validationInfo.validationResult) {
            switch (validationInfo.validationResult.split('.')[1]) {
                case 'notValidated':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'IMT', 'Not validated') + ')' +
                        '<img style="vertical-align: top; margin-left: 5px" width="16" height="16" src="../sky/build/resources/images/shared/Not-validated.png"/>';
                    break;
                case 'suspect':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.suspect', 'IMT', 'Suspect') + ')' +
                        '<img style="vertical-align: top; margin-left: 5px" width="16" height="16" src="../sky/build/resources/images/shared/Suspect.png"/>';
                    break;
                case 'ok':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notsuspect', 'IMT', 'Not suspect') + ')';
                    if (!me.channels && validationInfo.isConfirmed) {
                        validationResultText += '<span style="margin-left: 5px; vertical-align: top" class="icon-checkmark3"</span>';
                    }
                    break;
            }
        }

        if (!Ext.isEmpty(value)) {
            formatValue = Uni.Number.formatNumber(value.toString(), -1);
            return !Ext.isEmpty(formatValue) ? formatValue + ' ' + measurementType + ' ' + validationResultText : '';
        } else {
            if(type === 'main'){
                return Uni.I18n.translate('general.missingx', 'IMT', 'Missing {0}',[validationResultText], false);
            } else {
                return '-';
            }
        }
    },

    initComponent: function () {
        var me = this,
            generalItems = [],
            valuesItems = [];

        generalItems.push(
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'IMT', 'Interval'),
                name: 'interval',
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(value.start)),Uni.DateTime.formatTimeLong(new Date(value.start))])
                        + ' - ' +
                        Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(value.end)),Uni.DateTime.formatTimeLong(new Date(value.end))])
                        : '';
                },
                htmlEncode: false
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'IMT', 'Reading time'),
                name: 'readingTime',
                renderer: function (value, field) {
                    return value ? Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(value)), Uni.DateTime.formatTimeLong(new Date(value))]) : '';
                }
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
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.validationstatus.title', 'IMT', 'Validation status'),
                name: 'validationStatus',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.active', 'IMT', 'Active') :
                        Uni.I18n.translate('general.inactive', 'IMT', 'Inactive')
                }
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.dataValidated.title', 'IMT', 'Data validated'),
                itemId: 'readingDataValidated',
                htmlEncode: false,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('validationResults.dataValidatedYes', 'IMT', 'Yes') :
                        Uni.I18n.translate('validationResults.dataValidatedNo', 'IMT', 'No')
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.readingQualities', 'IMT', 'Reading qualities'),
                itemId: 'generalReadingQualities',
                htmlEncode: false
            }
        );

        valuesItems.push(
            {
                xtype: 'fieldcontainer',
                labelWidth: 200,
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'IMT', 'Value'),
                layout: 'hbox',
                items: [
                    {
                        xtype: 'displayfield',
                        name: 'value',
                        renderer: function (value) {
                            return me.setValueWithResult(value, 'main');
                        }
                    },
//                        {
//                            xtype: 'edited-displayfield',
//                            name: 'mainModificationState',
//                            margin: '0 0 0 10'
//                        }
                ]
            },
            {
                xtype: 'displayfield',
                labelWidth: 200,
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'IMT', 'Reading qualities'),
                itemId: 'mainReadingQualities',
                htmlEncode: false
            }//,
//                {
//                    xtype: 'fieldcontainer',
//                    labelWidth: 200,
//                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.bulkValue', 'IMT', 'Bulk value'),
//                    layout: 'hbox',
//                    items: [
//                        {
//                            xtype: 'displayfield',
//                            name: 'collectedValue',
//                            renderer: function (value) {
//                                return me.setValueWithResult(value, 'bulk');
//                            }
//                        },
//                        {
//                            xtype: 'edited-displayfield',
//                            name: 'bulkModificationState',
//                            margin: '0 0 0 10'
//                        }
//                    ]
//                },
//                {
//                    xtype: 'displayfield',
//                    labelWidth: 200,
//                    fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'IMT', 'Reading qualities'),
//                    itemId: 'bulkReadingQualities',
//                    htmlEncode: false
//                }
        );

        me.items = [
            {
                title: Uni.I18n.translate('devicechannelsdata.generaltab.title', 'IMT', 'General'),
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
                title: Uni.I18n.translate('devicechannelsdata.readingvaluetab.title', 'IMT', 'Reading values'),
                items: {
                    xtype: 'form',
                    itemId: 'values-panel',
                    frame: true,
                    items: valuesItems,
                    layout: 'vbox'
                }
            }
            ];
        me.callParent(arguments);
    }
});