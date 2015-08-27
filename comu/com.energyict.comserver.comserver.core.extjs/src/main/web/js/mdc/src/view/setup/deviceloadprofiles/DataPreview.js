Ext.define('Mdc.view.setup.deviceloadprofiles.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfilesDataPreview',
    itemId: 'deviceLoadProfilesDataPreview',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.DataActionMenu',
        'Uni.form.field.IntervalFlagsDisplay'
    ],
    layout: 'fit',
    frame: true,

    channels: null,

    /* Commented for now because of JP-5561
     tools: [
     {
     xtype: 'button',
     text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
     itemId: 'actionButton',
     iconCls: 'x-uni-action-iconD',
     menu: {
     xtype: 'deviceLoadProfilesDataActionMenu'
     }
     }
     ],
     */

    initComponent: function () {
        var me = this,
            channelsFields = [];

        Ext.Array.each(me.channels, function (channel) {
            channelsFields.push({
                xtype: 'fieldcontainer',
                fieldLabel: channel.name,
                itemId: 'channelFieldContainer' + channel.id,
                labelAlign: 'top',
                labelWidth: 400,
                layout: 'vbox',
                margin: '35 0 0 0',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                        itemId: 'channelValue' + channel.id
                    },
                    {
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.bulkValue', 'MDC', 'Bulk value'),
                        itemId: 'channelBulkValue' + channel.id,
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.dataValidated', 'MDC', 'Data validated'),
                        itemId: 'channelDataValidated' + channel.id,
                        htmlEncode: false
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
                        itemId: 'channelValidationResult' + channel.id
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.failedValidationRules', 'MDC', 'Failed validation rules'),
                        itemId: 'channelSuspectReason' + channel.id
                    }
                ]
            });
        });

        me.items = {
            xtype: 'form',
            itemId: 'deviceLoadProfilesDataPreviewForm',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '<span style="font-size: 12pt">' + Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General') + '</span>',
                    labelAlign: 'top',
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                            name: 'interval_formatted'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                            name: 'readingTime_formatted'
                        },
                        {
                            xtype: 'interval-flags-displayfield'
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '<span style="font-size: 12pt">' + Uni.I18n.translate('deviceloadprofiles.validation', 'MDC', 'Validation') + '</span>',
                    labelAlign: 'top',
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('device.registerData.validationStatus', 'MDC', 'Validation status'),
                            name: 'validationActive',
                            renderer: function (value) {
                                if (value) {
                                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                                } else {
                                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                                }
                            }
                        }
                    ]
                }
            ],

            loadRecord: function (record) {
                var form = this,
                    fields = form.query('[isFormField=true]');
                Ext.Array.each(me.channels, function (channel) {
                    if (form.down('#channelFieldContainer' + channel.id)) {
                        form.down('#channelFieldContainer' + channel.id).destroy();
                    }
                });
                form.add(channelsFields);
                Ext.Array.each(fields, function (field) {
                    var value = record.get(field.name);
                    if (value) {
                        field.setValue(value);
                    }
                });

                Ext.Array.each(me.channels, function (channel) {

                    if (!Ext.isEmpty(record.get('channelData')[channel.id])) {
                        form.down('#channelValue' + channel.id).setValue(record.data.channelData[channel.id] + ' ' + channel.unitOfMeasure.unit);
                    } else {
                        form.down('#channelValue' + channel.id).setValue(Uni.I18n.translate('general.missing', 'MDC', 'Missing'));
                    }

                    if (!Ext.isEmpty(record.get('channelCollectedData'))) {
                        var channelBulkValueField = form.down('#channelBulkValue' + channel.id);
                        channelBulkValueField.setValue(
                            !Ext.isEmpty(record.get('channelCollectedData')[channel.id])
                                ? Uni.Number.formatNumber(record.get('channelCollectedData')[channel.id], -1) + ' ' + channel.unitOfMeasure.unit
                                : Uni.I18n.translate('general.missing', 'MDC', 'Missing')
                        );
                        channelBulkValueField.show();
                    }

                    if (record.data.channelValidationData[channel.id]) {

                        record.data.channelValidationData[channel.id].dataValidated ? form.down('#channelDataValidated' + channel.id).setValue(Uni.I18n.translate('general.yes', 'MDC', 'Yes')) : form.down('#channelDataValidated' + channel.id).setValue(Uni.I18n.translate('general.no', 'MDC', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>');

                        switch (record.data.channelValidationData[channel.id].validationResult) {
                            case 'validationStatus.notValidated':
                                form.down('#channelValidationResult' + channel.id).hide();
                                break;
                            case 'validationStatus.ok':
                                form.down('#channelValidationResult' + channel.id).setValue(Uni.I18n.translate('general.notSuspect', 'MDC', 'Not suspect'));
                                break;
                            case 'validationStatus.suspect':
                                form.down('#channelValidationResult' + channel.id).setValue(Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect') + ' ' + '<span class="icon-validation icon-validation-red"></span>');
                                break;
                            default:
                                form.down('#channelValidationResult' + channel.id).hide();
                                break;
                        }

                        if (!Ext.isEmpty(record.data.channelValidationData[channel.id].validationRules)) {
                            var str = '',
                                prop,
                                failEqualDataValue,
                                intervalFlagsValue = '';
                            Ext.Array.each(record.data.channelValidationData[channel.id].validationRules, function (rule) {
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
                            form.down('#channelSuspectReason' + channel.id).setValue(str);
                        } else {
                            form.down('#channelSuspectReason' + channel.id).hide();
                        }
                    } else {
                        form.down('#channelValidationResult' + channel.id).hide();
                        form.down('#channelDataValidated' + channel.id).setValue(Uni.I18n.translate('general.no', 'MDC', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>');
                        form.down('#channelValidationResult' + channel.id).hide();
                        form.down('#channelSuspectReason' + channel.id).hide();
                    }
                });
            }
        };
        me.callParent(arguments);
    }
});
