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

    frame: false,

    updateForm: function (record) {
        var me = this,
            intervalEnd = record.get('interval_end'),
            title = Uni.DateTime.formatDateLong(intervalEnd) + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' ' + Uni.DateTime.formatTimeLong(intervalEnd),
            mainValidationInfo = record.getValidationInfo().getMainValidationInfo(),
            bulkValidationInfo = record.getValidationInfo().getBulkValidationInfo();

        Ext.suspendLayouts();
        me.down('#general-panel').setTitle(title);
        me.down('#values-panel').setTitle(title);
        me.down('form').loadRecord(record);
        me.setReadingQualities(me.down('#mainReadingQualities'), mainValidationInfo);
        me.setReadingQualities(me.down('#bulkReadingQualities'), bulkValidationInfo);
        me.setGeneralReadingQualities(me.down('#generalReadingQualities'), me.up('tabbedDeviceChannelsView').channel.get('validationInfo'));
        me.down('#readingDataValidated').setValue(record.getValidationInfo().get('dataValidated'));
        Ext.resumeLayouts(true);
    },

    setReadingQualities: function (field, info) {
        var me = this,
            estimatedRule,
            estimatedRuleName;

        field.show();
        if (!Ext.isEmpty(info.get('validationRules'))) {
            me.setValidationRules(me.down('#mainReadingQualities'), info.get('validationRules'));
        } else if (info.get('estimatedByRule')) {
            estimatedRule = info.get('estimatedByRule');
            estimatedRuleName = '<a href="#/administration/estimationrulesets/' + estimatedRule.ruleSetId + '/rules/' + estimatedRule.id + '">' + estimatedRule.name + '</a>';
            field.setValue(Uni.I18n.translate('deviceChannelData.estimatedAccordingTo', 'MDC', 'Estimated according to') + ' ' + estimatedRuleName);
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
            url;
        if (!Ext.isEmpty(value.suspectReason)) {
            field.show();
            Ext.Array.each(value.suspectReason, function (rule) {
                if (rule.key.deleted) {
                    result += Ext.String.htmlEncode(rule.key.name) + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') + ' - ' + rule.value + ' ' + Uni.I18n.translate('general.suspects', 'MDC', 'suspects') + '<br>';
                } else {
                    if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                        url = me.up('tabbedDeviceChannelsView').router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({ruleSetId: rule.key.ruleSetVersion.ruleSet.id, versionId: rule.key.ruleSetVersion.id, ruleId: rule.key.id});
                        result += '<a href="' + url + '"> ' + Ext.String.htmlEncode(rule.key.name) + '</a>';
                    } else {
                        result = Ext.String.htmlEncode(rule.key.name);
                    }
                }   result += ' - ' + Ext.String.htmlEncode(rule.value) + ' ' + Uni.I18n.translate('general.suspects', 'MDC', 'suspects') + '<br>';
            });
            field.setValue(result);
        } else {
            field.hide();
        }
    },

    setValueWithResult: function (value, type) {
        var me = this,
            record = me.down('form').getRecord(),
            measurementType = me.channelRecord.get('unitOfMeasure'),
            validationResult = record.get(type + 'ValidationInformation').validationResult,
            validationResultText = '',
            formatValue;

        if (validationResult) {
            switch (validationResult.split('.')[1]) {
                case 'notValidated':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated') + ')' +
                        '<img style="position: absolute; margin-left: 5px" width="16" height="16" src="../sky/build/resources/images/shared/Not-validated.png"/>&nbsp;&nbsp;&nbsp;&nbsp;';
                    break;
                case 'suspect':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.suspect', 'MDC', 'Suspect') + ')' +
                        '<img style="position: absolute; margin-left: 5px" width="16" height="16" src="../sky/build/resources/images/shared/Suspect.png"/>&nbsp;&nbsp;&nbsp;&nbsp;';
                    break;
                case 'ok':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notsuspect', 'MDC', 'Not suspect') + ')';
                    break;
            }
        }

        if (!Ext.isEmpty(value)) {
            formatValue = Uni.Number.formatNumber(value, -1);
            return !Ext.isEmpty(formatValue) ? formatValue + ' ' + measurementType + ' ' + validationResultText : '';
        } else {
            return Uni.I18n.translate('general.missing', 'MDC', 'Missing') + ' ' + validationResultText;
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
                name: 'intervalFlags'
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.validationstatus.title', 'MDC', 'Validation status'),
                name: 'validationStatus',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('devicechannelsreadings.validationstatus.active', 'MDC', 'Active') :
                        Uni.I18n.translate('devicechannelsreadings.validationstatus.inactive', 'MDC', 'Inactive')
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
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
                itemId: 'generalReadingQualities',
                htmlEncode: false
            }
        );

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
                            layout: 'vbox',
                            title: 'sdfsd'
                        }
                    }
                ]
            }
        };
        me.callParent(arguments);
    }
});