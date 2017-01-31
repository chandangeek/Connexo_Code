/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceCommunicationTaskPreview',
    itemId: 'deviceCommunicationTaskPreview',

    requires: [
        'Mdc.view.setup.property.PropertyView',
        'Mdc.util.ScheduleToStringConverter',
        'Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskActionMenu'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('general.details','MDC','Details'),

    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'actionsPreviewBtn',
            menu: {
                xtype: 'device-communication-task-action-menu'
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
                    html: '<h4>' + Uni.I18n.translate('deviceCommunicationTask.noCommunicationTaskSelected', 'MDC', 'No communication task selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('deviceCommunicationTask.selectCommunicationTask', 'MDC', 'Select a communication task to see its details') + '</h5>'
                }
            ]
        },
        {
            xtype: 'form',
            border: false,
            itemId: 'deviceCommunicationTaskPreviewForm',
            layout: {
                type: 'vbox'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column',
                        align: 'stretch'
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
                                labelWidth: 250,
                                renderer: function(value){
                                    return value?Ext.String.htmlEncode(value):'-';
                                }
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'comTask',
                                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                    renderer: function (value) {
                                        return Ext.String.htmlEncode(value.name);
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionMethod',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.connectionMethod', 'MDC', 'Connection method'),
                                    renderer: function(value){
                                        if(value!==''){
                                            if(!this.up('form').getRecord().data.connectionDefinedOnDevice){
                                                return '<tpl data-qtip=\''+ Uni.I18n.translate('deviceCommunicationTask.connectionNotDefinedOnDevice', 'MDC', 'This connection method is not defined on the device yet') + '\'><span class="icon-target ct-result ct-failure" style="display:inline-block; color:rgba(255, 0, 0, 0.3);"></span><span style="position: relative; left:5px;">' + Ext.String.htmlEncode(value) + '</span></tpl>'
                                            } else {
                                                return Ext.String.htmlEncode(value);
                                            }
                                        }

                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionStrategy',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.connectionStrategy', 'MDC', 'Connection strategy')

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'nextCommunication',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.nextCommunication', 'MDC', 'Next communication'),
                                    renderer: function (value) {
                                        if (value) {
                                            return Uni.DateTime.formatDateTimeLong(new Date(value));
                                        } else {
                                            return '-';
                                        }
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'lastCommunicationStart',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.lastCommunicationStart', 'MDC', 'Last communication start'),
                                    renderer: function (value) {
                                        if (value) {
                                            return Uni.DateTime.formatDateTimeLong(new Date(value));
                                        } else {
                                            return '-';
                                        }
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'status',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.status', 'MDC', 'Status')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'urgency',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.urgency', 'MDC', 'Urgency')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'securitySettings',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.securitySettings', 'MDC', 'Security settings')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'protocolDialect',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.protocolDialect', 'MDC', 'Protocol dialect')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'ignoreNextExecutionSpecsForInbound',
                                    fieldLabel: Uni.I18n.translate('communicationtasks.task.ignoreNextExecutionSpecsForInbound', 'MDC', 'Always execute for inbound'),
                                    renderer: function (value) {
                                        if (value === true) {
                                            return Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                                        }
                                        return Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
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
                                labelWidth: 250,
                                renderer: function(value){
                                    return value?value:'-';
                                }
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'temporalExpression',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.frequency', 'MDC', 'Frequency'),
                                    renderer: function (value) {
                                        if(value){
                                            return Mdc.util.ScheduleToStringConverter.convert(value);
                                        } else {
                                            return '-';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'scheduleType',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.scheduleType', 'MDC', 'Schedule type')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'scheduleName',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.scheduleName', 'MDC', 'Schedule name'),
                                    renderer: function (value) {
                                        if (value) {
                                            return Ext.String.htmlEncode(value);
                                        } else {
                                            return '-';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'plannedDate',
                                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.plannedDate', 'MDC', 'Planned date'),
                                    renderer: function (value) {
                                        if (value) {
                                            return Uni.DateTime.formatDateTimeLong(new Date(value));
                                        } else {
                                            return '-';
                                        }
                                    }
                                }
                            ]
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