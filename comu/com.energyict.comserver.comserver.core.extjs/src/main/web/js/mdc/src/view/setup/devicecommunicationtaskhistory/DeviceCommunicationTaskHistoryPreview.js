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
                      //  xtype: 'device-communication-task-history-action-menu'
                        items: [
                            {
                                text: Uni.I18n.translate('devicecommunicationtaskhistory.viewCommunicationLog', 'MDC', 'View communication log'),
                                itemId: 'viewCommunicationLog',
                                action: 'viewCommunicationLog'

                            }
                        ]
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
                                    renderer: function(value){
                                        if(value && value!==''){
                                            var data = this.up('form').getRecord().data;

                                            var link = '#/devices/' + data.comSession.device.id
                                                + '/connectionmethods/' + data.comSession.connectionMethod.id
                                                + '/history/' + data.comSession.id
                                                + '/viewlog' +
                                                '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22connections%22%2C%22communications%22%5D%7D'


                                            return '<a href="'+link+'">'+ value.connectionMethod.name + '</a>'
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
            tools: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    iconCls: 'x-uni-action-iconD',
                    menu: {
                        items: [
                            {
                                text: Uni.I18n.translate('devicecommunicationtaskhistory.viewConnectionLog', 'MDC', 'View connection log'),
                                itemId: 'viewConnectionLog',
                                action: 'viewConnectionLog'
                            }
                        ]
                    }
                }
            ],
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
                                    itemId: 'comPort',
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
                                        var template = '';
                                        if (val.numberOfSuccessfulTasks || val.numberOfFailedTasks || val.numberOfIncompleteTasks) {
                                            template += '<tpl><img src="/apps/dsh/resources/images/widget/running.png" title="Success" class="ct-result ct-success"><span style="position: relative; top: -3px; left: 4px">' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</span><br></tpl>';
                                            template += '<tpl><img src="/apps/dsh/resources/images/widget/blocked.png" title="Failed" class="ct-result ct-failure"><span style="position: relative; top: -3px; left: 4px">' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0')  + '</span><br></tpl>';
                                            template += '<tpl><img src="/apps/dsh/resources/images/widget/stopped.png" title="Not executed" class="ct-result ct-incomplete"><span  style="position: relative; top: -3px; left: 4px">' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</span></tpl>';
                                        }
                                        return template;
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




