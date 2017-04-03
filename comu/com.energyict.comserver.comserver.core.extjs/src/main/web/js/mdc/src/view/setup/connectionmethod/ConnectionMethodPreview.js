/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.connectionMethodPreview',
    itemId: 'connectionMethodPreview',
    requires: [
        'Uni.property.form.Property',
        'Mdc.model.ConnectionMethod',
        'Mdc.view.setup.connectionmethod.ConnectionMethodActionMenu'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('general.details','MDC','Details'),

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'connection-method-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'panel',
            border: false,
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('connectionmethod.noConnectionMethodSelected', 'MDC', 'No connection method selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('connectionmethod.details.selectConnectionMethod', 'MDC', 'Select a connection method to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'connectionMethodPreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
                    },
                    items: [
                        {
                            xtype: 'container',
                            columnWidth: 0.49,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                    itemId: 'deviceName'

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'isDefault',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.default', 'MDC', 'Default'),
                                    renderer: function(value){
                                        return value? Uni.I18n.translate('general.yes', 'MDC', 'Yes'):Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionTypePluggableClass',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionType', 'MDC', 'Connection type')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'displayDirection',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.direction', 'MDC', 'Direction')
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('communicationtasks.task.protocolDialectConfigurationProperties', 'MDC', 'Protocol dialect'),
                                    name: 'protocolDialectConfigurationProperties',
                                    renderer: function (value) {
                                        return Ext.String.htmlEncode(value.displayName);
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'comPortPool',
                                    fieldLabel: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool')
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.49,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionStrategyInfo',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionStrategy', 'MDC', 'Connection strategy'),
                                    renderer: function (value, field) {
                                        if (!Ext.isEmpty(value)) {
                                            field.show();
                                            return value.localizedValue;
                                        } else {
                                            field.hide();
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'temporalExpression',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionSchedule', 'MDC', 'Connection schedule'),
                                    renderer: function (value, field) {
                                        var record = this.up('form').getRecord(),
                                            isOutbound = record && record.get('direction') === 'Outbound',
                                            connectionStrategyInfo = record && record.get('connectionStrategyInfo'),
                                            isMinimizeConnections = connectionStrategyInfo && connectionStrategyInfo.connectionStrategy === 'MINIMIZE_CONNECTIONS';
                                        if (isOutbound && isMinimizeConnections) {
                                            field.show();
                                            return Mdc.util.ScheduleToStringConverter.convert(record.get('temporalExpression'));
                                        } else {
                                            field.hide();
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'rescheduleRetryDelay',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.rescheduleRetryDelay', 'MDC', 'Retry delay'),
                                    renderer: function (value, field) {
                                        var record = this.up('form').getRecord();
                                        if (record && (record.get('direction') == 'Inbound')) {
                                            field.hide();
                                        } else if (value) {
                                            field.show();
                                            return value.count + ' ' + (value.translatedTimeUnit ? value.translatedTimeUnit : value.timeUnit);
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionWindow',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionWindow', 'MDC', 'Connection window'),
                                    renderer: function (value, field) {
                                        var record = this.up('form').getRecord();
                                        if (value) {
                                            field.show();
                                            if (value.start || value.end) {
                                                var startMinutes = (value.start/3600 | 0),
                                                    startSeconds = (value.start/60 - startMinutes*60),
                                                    endMinutes = (value.end/3600 | 0),
                                                    endSeconds = (value.end/60 - endMinutes*60);

                                                var addZeroIfOneSymbol = function (timeCount) {
                                                    var timeInString = timeCount.toString();
                                                    if (timeInString.length === 1) {
                                                        timeInString = '0' + timeInString;
                                                    }
                                                    return timeInString;
                                                };
                                                return Uni.I18n.translate('connectionmethod.between', 'MDC', 'Between') + ' ' + addZeroIfOneSymbol(startMinutes) + ':' + addZeroIfOneSymbol(startSeconds)  + ' ' + Uni.I18n.translate('general.and', 'MDC', 'And').toLowerCase() + ' ' + addZeroIfOneSymbol(endMinutes) + ':' + addZeroIfOneSymbol(endSeconds);

                                            } else if (record && (record.get('direction') == 'Inbound')) {
                                                field.hide();
                                            } else {
                                                return Uni.I18n.translate('connectionmethod.norestriction', 'MDC', 'No restrictions');
                                            }
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'numberOfSimultaneousConnections',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.numberOfSimultaneousConnections', 'MDC', 'Number of simultaneous connections'),
                                    renderer: function (value, field) {
                                        var record = this.up('form').getRecord(),
                                            isOutbound = record && record.get('direction') === 'Outbound',
                                            connectionStrategyInfo = record && record.get('connectionStrategyInfo'),
                                            isASAP = connectionStrategyInfo && connectionStrategyInfo.connectionStrategy === 'AS_SOON_AS_POSSIBLE';
                                        if (isOutbound && isASAP) {
                                            field.show();
                                            return value;
                                        } else {
                                            field.hide();
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'connectionDetailsTitle',
                    hidden: true,
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    defaults: {
                        labelWidth: 250,
                        labelAlign: 'left'
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            name: 'connectionTypePluggableClass',
                            fieldLabel: '<h3>' +  Uni.I18n.translate('deviceconnectionmethod.connectionDetails', 'MDC', 'Connection details') + '</h3>',
                            renderer: function() {
                                return ''; // No dash!
                            }
                        }
                    ]
                },
                {
                    xtype: 'property-form',
                    isEdit: false,
                    layout: 'column',
                    defaults: {
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 250,
                        columnWidth: 0.49
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
     }
});


