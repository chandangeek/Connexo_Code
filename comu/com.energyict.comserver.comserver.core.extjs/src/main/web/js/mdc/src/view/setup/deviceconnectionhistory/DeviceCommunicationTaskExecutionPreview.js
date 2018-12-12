/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskExecutionPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceCommunicationTaskExecutionPreview',
    itemId: 'deviceCommunicationTaskExecutionPreview',
    requires: [
        'Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskGridActionMenu',
        'Uni.form.field.Duration'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    title: Uni.I18n.translate('general.details','MDC','Details'),

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'mdc-device-communication-task-grid-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            border: false,
            width: '100%',
            layout: {
                type: 'vbox'
            },
            itemId: 'deviceCommunicationTaskExecutionPreviewForm',
            items: [
                {
                    xtype: 'displayfield',
                    htmlEncode: false,
                    name: 'communicationSummary',
                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.communicationSummary', 'MDC', 'Communication summary'),
                    itemId: 'communication-summary',
                    width: '100%',
                    labelWidth: 250
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    width: 250,
                    items: [
                        {
                            xtype: 'component',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            itemId: 'btn-show-communication-details',
                            text: Uni.I18n.translate('deviceconnectionhistory.showCommunicationDetails','MDC','Show communication details'),
                            action: 'showCommunicationDetails'
                        }
                    ]
                },
                {
                    xtype: 'form',
                    border: false,
                    width: '100%',
                    layout: {
                        type: 'column'
                        //    align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'container',
                            itemId: 'left-communication-details',
                            columnWidth: 0.49,
                            hidden: true,
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
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.comTask', 'MDC', 'Communication task'),
                                    itemId: 'name'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'device',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.device', 'MDC', 'Device'),
                                    itemId: 'device',
                                    renderer: function(device){
                                        return device!=='' ? '<a href="#/devices/'+device.name+'">' + Ext.String.htmlEncode(device.name) + '</a>' : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceType',
                                    fieldLabel: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                                    itemId: 'deviceType',
                                    renderer: function(deviceType){
                                        return deviceType!=='' ? '<a href="#/administration/devicetypes/'+deviceType.id+'">' + Ext.String.htmlEncode(deviceType.name) + '</a>' : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceConfiguration',
                                    fieldLabel: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                                    itemId: 'deviceConfiguration',
                                    renderer: function(deviceConfiguration){
                                        return deviceConfiguration!='' ? '<a href="#/administration/devicetypes/'+deviceConfiguration.deviceTypeId+'/deviceconfigurations/' + deviceConfiguration.id+'">' + Ext.String.htmlEncode(deviceConfiguration.name) + '</a>' : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'comScheduleName',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.schedule', 'MDC', 'Schedule'),
                                    itemId: 'comScheduleName'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'comScheduleFrequency',
                                    itemId: 'comScheduleFrequency',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.frequency', 'MDC', 'Frequency'),
                                    renderer: function(value){
                                        return value!='' ? Mdc.util.ScheduleToStringConverter.convert(value) || Uni.I18n.translate('general.undefined', 'MDC', 'Undefined') : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'urgency',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.urgency', 'MDC', 'Urgency'),
                                    itemId: 'urgency'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'alwaysExecuteOnInbound',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.alwaysExecuteOnInbound', 'MDC', 'Always execute on inbound'),
                                    itemId: 'alwaysExecuteOnInbound',
                                    renderer: function(value){
                                        return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.50,
                            itemId: 'right-communication-details',
                            hidden: true,
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
                                    name: 'result',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                                    itemId: 'resultLink'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'startTime',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.startedOn', 'MDC', 'Started on'),
                                    itemId: 'startedOn',
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'finishTime',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.finishedOn', 'MDC', 'Finished on'),
                                    itemId: 'finishedOn',
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                                    }
                                },
                                {
                                    xtype: 'uni-form-field-duration',
                                    name: 'durationInSeconds',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                                    itemId: 'durationInSeconds',
                                    usesSeconds: true
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    width: 250,
                    items: [
                        {
                            xtype: 'component',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            itemId: 'btn-hide-communication-details',
                            text: Uni.I18n.translate('deviceconnectionhistory.hideCommunicationDetails','MDC','Hide communication details'),
                            action: 'hideCommunicationDetails',
                            hidden: true
                        }
                    ]
                }
            ]

        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});




