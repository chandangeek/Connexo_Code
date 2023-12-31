/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceConnectionMethodPreview',
    itemId: 'deviceConnectionMethodPreview',
    requires: [
        'Mdc.model.DeviceConnectionMethod',
        'Mdc.view.setup.property.PropertyView',
        'Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodActionMenu',
        'Mdc.util.ScheduleToStringConverter'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'device-connection-method-action-menu'
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
                    html: '<h4>' + Uni.I18n.translate('deviceconnectionmethod.noConnectionMethodSelected', 'MDC', 'No connection method selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('deviceconnectionmethod.details.selectConnectionMethod', 'MDC', 'Select a connection method to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'deviceConnectionMethodPreviewForm',
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
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.default', 'MDC', 'Default'),
                                    renderer: function (value) {
                                        return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'displayDirection',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.direction', 'MDC', 'Direction')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionType',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.connectionType', 'MDC', 'Connection type')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'status',
                                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                    dataIndex: 'status',
                                    renderer: function (value, b, record) {
                                        switch (value) {
                                            case 'connectionTaskStatusIncomplete':
                                                return Uni.I18n.translate('deviceconnectionmethod.status.incomplete', 'MDC', 'Incomplete');
                                            case 'connectionTaskStatusActive':
                                                return Uni.I18n.translate('general.active', 'MDC', 'Active');
                                            case 'connectionTaskStatusInActive':
                                                return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                                            default :
                                                return '';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'protocolDialectDisplayName',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.protocolDialect', 'MDC', 'Protocol dialect')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionFunctionInfo',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionFunction', 'MDC', 'Connection function'),
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
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.connectionStrategy', 'MDC', 'Connection strategy'),
                                    renderer: function (value, field) {
                                        var record = this.up('form').getRecord();
                                        if (record && (record.get('direction') === 'Inbound')) {
                                            field.hide();
                                        }
                                        else if (value) {
                                            field.show();
                                            return value['localizedValue'];
                                       }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'nextExecutionSpecs',
                                    fieldLabel: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                                    renderer: function (value, field) {
                                        var record = this.up('form').getRecord(),
                                            isMinimizeConnections,
                                            connectionStrategyInfo = record && record.get('connectionStrategyInfo');
                                        isMinimizeConnections = connectionStrategyInfo && connectionStrategyInfo.connectionStrategy === 'MINIMIZE_CONNECTIONS';
                                        if (!isMinimizeConnections) {
                                            field.hide();
                                        } else {
                                            field.show();
                                        }
                                        return Mdc.util.ScheduleToStringConverter.convert(value) || '-';
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionWindow',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionWindow', 'MDC', 'Connection window'),
                                    renderer: function (value, field) {
                                        var record = this.up('form').getRecord();
                                        if (record && (record.get('direction') === 'Inbound')) {
                                            field.hide();
                                            return '-';
                                        } else {
                                            field.show();
                                        }
                                        if (value) {
                                            if (value.start || value.end) {
                                                var startMinutes = (value.start / 3600 | 0),
                                                    startSeconds = (value.start / 60 - startMinutes * 60),
                                                    endMinutes = (value.end / 3600 | 0),
                                                    endSeconds = (value.end / 60 - endMinutes * 60);

                                                var addZeroIfOneSymbol = function (timeCount) {
                                                    var timeInString = timeCount.toString();

                                                    if (timeInString.length === 1) {
                                                        timeInString = '0' + timeInString;
                                                    }
                                                    return timeInString;
                                                };

                                                return Uni.I18n.translate('connectionmethod.between', 'MDC', 'Between') + ' ' + addZeroIfOneSymbol(startMinutes) + ':' + addZeroIfOneSymbol(startSeconds) + ' ' + Uni.I18n.translate('general.and', 'MDC', 'And').toLowerCase() + ' ' + addZeroIfOneSymbol(endMinutes) + ':' + addZeroIfOneSymbol(endSeconds);

                                            } else {
                                                return Uni.I18n.translate('connectionmethod.norestriction', 'MDC', 'No restrictions');
                                            }
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'numberOfSimultaneousConnections',
                                    itemId: 'numberOfSimultaneousConnections',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionmethod.numberOfSimultaneousConnections', 'MDC', 'Number of simultaneous connections')
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
                            fieldLabel: Uni.I18n.translate('deviceconnectionmethod.connectionDetails', 'MDC', 'Connection details'),
                            renderer: function () {
                                return ''; // No dash!
                            }
                        }
                    ]
                },
                {
                    xtype: 'property-form',
                    itemId: 'propertyForm',
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


