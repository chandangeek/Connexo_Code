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

    title: 'Details',

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
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
                    html: '<h4>' + Uni.I18n.translate('devicecommunicationTask.noCommunicationTaskSelected', 'MDC', 'No communication task selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('devicecommunicationTask.selectCommunicationTask', 'MDC', 'Select a communication task to see its details') + '</h5>'
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
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'comTask',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.name', 'MDC', 'Name'),
                                    renderer: function (value) {
                                        return value.name;
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionMethod',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.connectionMethod', 'MDC', 'Connection method')

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionStrategy',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.connectionStrategy', 'MDC', 'Connection strategy')

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'nextCommunication',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.nextCommunication', 'MDC', 'Next communication'),
                                    renderer: function (value) {
                                        if (value) {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'lastCommunication',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.lastCommunicationStart', 'MDC', 'Last communication start'),
                                    renderer: function (value) {
                                        if (value) {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'status',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.currentStatus', 'MDC', 'Current status')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'urgency',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.urgency', 'MDC', 'Urgency')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'securitySettings',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.securitySettings', 'MDC', 'Security settings')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'protocolDialect',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.protocolDialect', 'MDC', 'Protocol dialect')
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
                                    name: 'temporalExpression',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.frequency', 'MDC', 'Frequency'),
                                    renderer: function (value) {
                                        if(value){
                                            return Mdc.util.ScheduleToStringConverter.convert(value);
                                        } else {
                                            return '';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'scheduleType',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.scheduleType', 'MDC', 'Schedule type')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'scheduleName',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.scheduleName', 'MDC', 'Schedule name')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'plannedDate',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationTask.plannedDate', 'MDC', 'Planned date'),
                                    renderer: function (value) {
                                        if (value) {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
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