Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryPreview', {
    extend: 'Ext.container.Container',

    alias: 'widget.deviceCommunicationTaskHistoryPreview',
    itemId: 'deviceCommunicationTaskHistoryPreview',
    requires: [

    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    title: 'Details',


    items: [
        {
            xtype: 'panel',
            title: Uni.I18n.translate('devicecommunicationtaskhistory.communicationTask', 'MDC', 'Communication task'),
            frame: true,
            layout: {
                type: 'vbox'
            },
            tools: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    iconCls: 'x-uni-action-iconD',
                    menu: {
                        xtype: 'device-communication-task-history-action-menu'
                    }
                }
            ],
            items: [
                {
                    xtype: 'form',
                    border: false,
                    layout: {
                        type: 'column'
//                align: 'stretch'
                    },
                    itemId: 'deviceCommunicationTaskHistoryPreviewForm',
//            layout: {
//                type: 'vbox',
//                align: 'stretch'
//            },
//            items: [
//                {
//                    columnWidth: 0.50,
//                    xtype: 'container',
//                    layout: {
//                        type: 'column'
////                        align: 'stretch'
//                    },
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
                                    name: 'startTime',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.startedOn', 'MDC', 'Started on'),
                                    itemId: 'startedOn',
                                    renderer: function (value, metadata) {
                                        if (value !== '') {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'finishTime',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.finishedOn', 'MDC', 'Finished on'),
                                    itemId: 'finishedOn',
                                    renderer: function (value, metadata) {
                                        if (value !== '') {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'startTime',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.startedOn', 'MDC', 'Started on'),
                                    itemId: 'startedOn',
                                    renderer: function (value, metadata) {
                                        if (value !== '') {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'durationInSeconds',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.duration', 'MDC', 'Duration'),
                                    itemId: 'durationInSeconds',
                                    renderer: function (value) {
                                        if (value !== '') {
                                            return value + ' ' + Uni.I18n.translate('general.seconds', 'MDC', 'seconds');
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    itemId: 'result',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.result', 'MDC', 'Result'),
                                    name: 'result'
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.50,
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
                                    name: 'comSession',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.connectionUsed', 'MDC', 'Connection used'),
                                    itemId: 'comSession',
                                    renderer: function (value) {
                                        if (value) {
                                            return value.connectionMethod.name;
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
        },
        {
            xtype: 'panel',
            title: 'Details',
            itemId: 'deviceConnectionHistoryPreviewPanel',
            style: {
                'margin-top': '32px'
            },
            frame: true,
            layout: {
                type: 'vbox'
            },
            items: [
                {
                    xtype: 'form',
                    border: false,
                    layout: {
                        type: 'column'
//                align: 'stretch'
                    },
                    itemId: 'deviceConnectionHistoryPreviewForm',
//            layout: {
//                type: 'vbox',
//                align: 'stretch'
//            },
//            items: [
//                {
//                    columnWidth: 0.50,
//                    xtype: 'container',
//                    layout: {
//                        type: 'column'
////                        align: 'stretch'
//                    },
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
                                    name: 'device',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.device', 'MDC', 'Device'),
                                    itemId: 'device',
                                    renderer: function (device) {
                                        return device !== '' ? '<a href="#/devices/' + device.id + '">' + device.name + '</a>' : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceType',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.deviceType', 'MDC', 'Device type'),
                                    itemId: 'deviceType',
                                    renderer: function (deviceType) {
                                        return deviceType !== '' ? '<a href="#/administration/devicetypes/' + deviceType.id + '">' + deviceType.name + '</a>' : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceConfiguration',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.deviceConfiguration', 'MDC', 'Device configuration'),
                                    itemId: 'deviceConfiguration',
                                    renderer: function (deviceConfiguration) {
                                        return deviceConfiguration != '' ? '<a href="#/administration/devicetypes/' + deviceConfiguration.deviceTypeId + '/deviceconfigurations/' + deviceConfiguration.id + '">' + deviceConfiguration.name + '</a>' : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'direction',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.direction', 'MDC', 'Direction')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionType',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.connectionType', 'MDC', 'Connection type')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'comServer',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.communicationServer', 'MDC', 'Communication server'),
                                    renderer: function (value) {
                                        return value.name;
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'comPort',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.communicationPort', 'MDC', 'Communication port')
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.50,
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
                                    name: 'status',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.status', 'MDC', 'Status')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'result',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.result', 'MDC', 'Result'),
                                    renderer: function (value) {
                                        return value ? value.displayValue : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.communicationTasks', 'DSH', 'Communication tasks'),
                                    name: 'comTaskCount',
                                    renderer: function (val) {
                                        return '<tpl><span class="fa fa-check fa-lg" style="color: green; width: 24px; vertical-align: 0% !important;"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '') + '</span><br></tpl>' +
                                            '<tpl><span class="fa fa-times fa-lg" style="color: red; width: 24px; vertical-align: 0% !important;"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '') + '<br></tpl>' +
                                            '<tpl><span class="fa fa-ban fa-lg" style="color: #333333; width: 24px; vertical-align: 0% !important"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '') + '</tpl>'
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'startedOn',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.startedOn', 'MDC', 'Started on'),
                                    renderer: function (value, metadata) {
                                        if (value !== '') {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'finishedOn',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.finishedOn', 'MDC', 'Finished on'),
                                    renderer: function (value, metadata) {
                                        if (value !== '') {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'durationInSeconds',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.duration', 'MDC', 'Duration'),
                                    renderer: function (value) {
                                        if (value !== '') {
                                            return value + ' ' + Uni.I18n.translate('general.seconds', 'MDC', 'seconds');
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




