/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    title: Uni.I18n.translate('general.details','MDC','Details'),


    items: [
        {
            xtype: 'panel',
            title: Uni.I18n.translate('devicecommunicationtaskhistory.communicationTask', 'MDC', 'Communication task'),
            frame: true,
            itemId: 'deviceCommunicationTaskHistoryPreviewPanel',
            layout: {
                type: 'vbox'
            },
            tools: [
                {
                    xtype: 'uni-button-action',
                    menu: {
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
                    xtype: 'displayfield',
                    htmlEncode: false,
                    name: 'communicationSummary',
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.communicationSummary', 'MDC', 'Communication summary'),
                    itemId: 'com-task-communication-summary',
                    labelWidth: 250
                },
                {
                    xtype: 'button',
                    itemId: 'btn-com-task-show-communication-details',
                    text: Uni.I18n.translate('devicecommunicationtaskhistory.showCommunicationDetails','MDC','Show communication details'),
                    action: 'showComTaskCommunicationDetails',
                    margin: '0 0 0 10'
                },
                {
                    xtype: 'form',
                    border: false,
                    width: '100%',
                    hidden: true,
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
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'finishTime',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.finishedOn', 'MDC', 'Finished on'),
                                    itemId: 'finishedOn',
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
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
                                            var data = this.up('form').getRecord().data,
                                                link = '#/devices/' + encodeURIComponent(data.comSession.device.id)
                                                    + '/connectionmethods/' + data.comSession.connectionMethod.id
                                                    + '/history/' + data.comSession.id
                                                    + '/viewlog'
                                                    + '?logLevels=Error&logLevels=Warning&logLevels=Information&communications=Connections&communications=Communications';

                                            return '<a href="' + link + '">' + Ext.String.htmlEncode(value.connectionMethod.name) + '</a>'
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
        },
        {
            xtype: 'panel',
            title: Uni.I18n.translate('general.details','MDC','Details'),
            itemId: 'deviceConnectionHistoryPreviewPanel',
            tools: [
                {
                    xtype: 'uni-button-action',
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
                    xtype: 'displayfield',
                    htmlEncode: false,
                    name: 'connectionSummary',
                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.connectionSummary', 'MDC', 'Connection summary'),
                    itemId: 'com-task-connection-summary',
                    labelWidth: 250
                },
                {
                    xtype: 'button',
                    itemId: 'btn-com-task-show-connection-details',
                    text: Uni.I18n.translate('devicecommunicationtaskhistory.showConnectionDetails','MDC','Show connection details'),
                    action: 'showComTaskConnectionDetails',
                    margin: '0 0 0 10'
                },
                {
                    xtype: 'form',
                    border: false,
                    hidden: true,
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
                                        return device !== '' ? '<a href="#/devices/' + device.name + '">' + Ext.String.htmlEncode(device.name) + '</a>' : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceType',
                                    fieldLabel: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                                    itemId: 'deviceType',
                                    renderer: function (deviceType) {
                                        return deviceType !== '' ? '<a href="#/administration/devicetypes/' + deviceType.id + '">' + Ext.String.htmlEncode(deviceType.name) + '</a>' : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deviceConfiguration',
                                    fieldLabel: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                                    itemId: 'deviceConfiguration',
                                    renderer: function (deviceConfiguration) {
                                        return deviceConfiguration != '' ? '<a href="#/administration/devicetypes/' + deviceConfiguration.deviceTypeId + '/deviceconfigurations/' + deviceConfiguration.id + '">' + Ext.String.htmlEncode(deviceConfiguration.name) + '</a>' : '-';
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
                                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'result',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.result', 'MDC', 'Result'),
                                    renderer: function (value) {
                                        return value ? value.displayValue : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                                    name: 'comTaskCount',
                                    cls: 'communication-tasks-status',
                                    renderer: function (val) {
                                        if (Ext.isEmpty(val)) {
                                            return '-';
                                        }
                                        var template = '',
                                            tooltipText = '';
                                        tooltipText += Uni.I18n.translatePlural(
                                            'device.connections.comTasksSuccessful', val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : 0, 'MDC',
                                            'No communication tasks successful', '1 communication task successful', '{0} communication tasks successful'
                                        );
                                        tooltipText += '<br>';
                                        tooltipText += Uni.I18n.translatePlural(
                                            'device.connections.comTasksFailed', val.numberOfFailedTasks ? val.numberOfFailedTasks : 0, 'MDC',
                                            'No communication tasks failed', '1 communication task failed', '{0} communication tasks failed'
                                        );
                                        tooltipText += '<br>';
                                        tooltipText += Uni.I18n.translatePlural(
                                            'device.connections.comTasksNotCompleted', val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : 0, 'MDC',
                                            'No communication tasks not completed', '1 communication task not completed', '{0} communication tasks not completed'
                                        );

                                        if (this.tooltip){
                                            this.tooltip.update(tooltipText);
                                        }else {
                                            this.tooltip = Ext.create('Ext.tip.ToolTip', {
                                                target: this.getEl(),
                                                html: tooltipText
                                            });
                                        }

                                        template += '<tpl><span class="icon-checkmark"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</tpl>';
                                        template += '<tpl><span class="icon-cross"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</tpl>';
                                        template += '<tpl><span  class="icon-stop2"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</tpl>';
                                        return template;
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'startedOn',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.startedOn', 'MDC', 'Started on'),
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'finishedOn',
                                    fieldLabel: Uni.I18n.translate('devicecommunicationhistory.finishedOn', 'MDC', 'Finished on'),
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
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




