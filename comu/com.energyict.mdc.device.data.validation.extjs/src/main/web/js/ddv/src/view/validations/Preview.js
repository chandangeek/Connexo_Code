/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.view.validations.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.ddv-validations-preview',
    requires: [],
    tools: [],

    items: [
        {
            xtype: 'form',
            itemId: 'validations-details-form',
            layout: 'column',
            items: [
                {
                    xtype: 'container',
                    columnWidth: 0.5,
                    layout: 'form',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            xtype: 'fieldcontainer',
                            labelAlign: 'top',
                            layout: 'vbox',
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 250
                            },
                            items: [
                                {
                                    fieldLabel: Uni.I18n.translate('validations.serialNumber', 'DDV', 'Serial number'),
                                    name: 'serialNumber',
                                    itemId: 'serial-number-validations-preview',
                                    renderer: function (value) {
                                        return value ? Ext.String.htmlEncode(value) : '-';
                                    }
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('validations.deviceType', 'DDV', 'Device type'),
                                    name: 'deviceType',
                                    itemId: 'deviceType-validations-preview'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('validations.deviceConfig', 'DDV', 'Configuration'),
                                    name: 'deviceConfig',
                                    itemId: 'deviceConfiguration-validations-preview'
                                }
                            ]
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('validations.suspectReadings', 'DDV', 'Suspect readings'),
                            labelAlign: 'top',
                            layout: 'vbox',
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 250
                            },
                            items: [
                                {
                                    fieldLabel: Uni.I18n.translate('validations.registerSuspects', 'DDV', 'Registers'),
                                    name: 'registerSuspects',
                                    itemId: 'registerSuspects-number-validations-preview'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('validations.channelSuspects', 'DDV', 'Channels'),
                                    name: 'channelSuspects',
                                    itemId: 'channelSuspects-validations-preview'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('validations.lastSuspect', 'DDV', 'Last suspect'),
                                    name: 'lastSuspect',
                                    itemId: 'last-suspect-validations-preview',
                                    renderer: function (value) {
                                        if (value) {
                                            return Uni.DateTime.formatDateLong(new Date(value));
                                        } else {
                                            return '-';
                                        }
                                    }
                                },
                            ]
                        }
                    ]
                },
                {
                    xtype: 'container',
                    columnWidth: 0.5,
                    layout: 'form',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                       /* {
                            name: 'lastValidation',
                            itemId: 'last-validation-validations-preview',
                            fieldLabel: Uni.I18n.translate('validations.lastValidation', 'DDV', 'Last validation'),
                            renderer: function (value) {
                                if (value) {
                                    return Uni.DateTime.formatDateTimeLong(value);
                                } else {
                                    return '-';
                                }
                            }
                        }, */
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('validations.typeOfSuspects', 'DDV', 'Type of suspects'),
                            labelAlign: 'top',
                            layout: 'vbox',
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 250
                            },
                            items: [
                                {
                                    fieldLabel: Uni.I18n.translate('validations.thresholdValidator', 'DDV', 'Threshold violation'),
                                    name: 'thresholdValidator',
                                    itemId: 'thresholdValidator-validations-preview',
                                    renderer: function (value) {
                                        return value
                                            ? Uni.I18n.translate('general.yes', 'DDV', 'Yes')
                                            : Uni.I18n.translate('general.no', 'DDV', 'No');
                                    }
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('validations.missingValuesValidator', 'DDV', 'Missing values'),
                                    name: 'missingValuesValidator',
                                    itemId: 'missingValuesValidator-validations-preview',
                                    renderer: function (value) {
                                        return value
                                            ? Uni.I18n.translate('general.yes', 'DDV', 'Yes')
                                            : Uni.I18n.translate('general.no', 'DDV', 'No');
                                    }
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('validations.readingQualitiesValidator', 'DDV', 'Interval state'),
                                    name: 'readingQualitiesValidator',
                                    itemId: 'readingQualitiesValidator-validations-preview',
                                    renderer: function (value) {
                                        return value
                                            ? Uni.I18n.translate('general.yes', 'DDV', 'Yes')
                                            : Uni.I18n.translate('general.no', 'DDV', 'No');
                                    }
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('validations.registerIncreaseValidator', 'DDV', 'Register increase'),
                                    name: 'registerIncreaseValidator',
                                    itemId: 'registerIncreaseValidator-validations-preview',
                                    renderer: function (value) {
                                        return value
                                            ? Uni.I18n.translate('general.yes', 'DDV', 'Yes')
                                            : Uni.I18n.translate('general.no', 'DDV', 'No');
                                    }
                                }
                            ]
                        },
                       /* {
                            itemId: 'type-of-suspects-validations-preview',
                            fieldLabel: Uni.I18n.translate('validations.typeOfSuspects', 'DDV', 'Type of suspects'),
                            renderer: function (value, field) {
                                return value;
                            }
                        }*/
                    ]
                }

            ]
        }
    ]
});