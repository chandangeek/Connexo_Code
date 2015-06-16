Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryPreview', {
    extend: 'Ext.container.Container',

    alias: 'widget.deviceCommunicationTaskHistoryPreview',
    itemId: 'deviceCommunicationTaskHistoryPreview',
    requires: [
        'Uni.form.field.Duration'
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
                    width: '100%',
                    layout: {
                        type: 'column'
                    },
                    itemId: 'deviceCommunicationTaskHistoryPreviewForm',
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
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'finishTime',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.finishedOn', 'MDC', 'Finished on'),
                                    itemId: 'finishedOn',
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                    }
                                },
                                {
                                    xtype: 'uni-form-field-duration',
                                    name: 'durationInSeconds',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.duration', 'MDC', 'Duration'),
                                    itemId: 'durationInSeconds',
                                    usesSeconds: true
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
                                        if (value && value !== '') {
                                            var data = this.up('form').getRecord().data;

                                            var link = '#/devices/' + encodeURIComponent(data.comSession.device.id)
                                                + '/connectionmethods/' + data.comSession.connectionMethod.id
                                                + '/history/' + data.comSession.id
                                                + '/viewlog'
                                                + '?logLevels=Error&logLevels=Warning&logLevels=Information&communications=Connections&communications=Communications';

                                            return '<a href="' + link + '">' + Ext.String.htmlEncode(value.connectionMethod.name) + '</a>'
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
                    width: '100%',
                    layout: {
                        type: 'column'
                    },
                    itemId: 'deviceConnectionHistoryPreviewForm',
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
                                        return device !== '' ? '<a href="#/devices/' + device.id + '">' + Ext.String.htmlEncode(device.name) + '</a>' : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceType',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.deviceType', 'MDC', 'Device type'),
                                    itemId: 'deviceType',
                                    renderer: function (deviceType) {
                                        return deviceType !== '' ? '<a href="#/administration/devicetypes/' + deviceType.id + '">' + Ext.String.htmlEncode(deviceType.name) + '</a>' : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceConfiguration',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.deviceConfiguration', 'MDC', 'Device configuration'),
                                    itemId: 'deviceConfiguration',
                                    renderer: function (deviceConfiguration) {
                                        return deviceConfiguration != '' ? '<a href="#/administration/devicetypes/' + deviceConfiguration.deviceTypeId + '/deviceconfigurations/' + deviceConfiguration.id + '">' + Ext.String.htmlEncode(deviceConfiguration.name) + '</a>' : '';
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
                                    htmlEncode: false,
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
                                    cls: 'communication-tasks-status',
                                    renderer: function (val) {
                                        var template = '';
                                        template += '<tpl><span class="icon-checkmark"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '<br></tpl>';
                                        template += '<tpl><span class="icon-close"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '<br></tpl>';
                                        template += '<tpl><span  class="icon-stop2"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</tpl>';
                                        return template;
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'startedOn',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.startedOn', 'MDC', 'Started on'),
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'finishedOn',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.finishedOn', 'MDC', 'Finished on'),
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                    }
                                },
                                {
                                    xtype: 'uni-form-field-duration',
                                    name: 'durationInSeconds',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.duration', 'MDC', 'Duration'),
                                    usesSeconds: true
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




