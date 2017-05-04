/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceConnectionHistoryPreview',
    itemId: 'deviceConnectionHistoryPreview',
    requires: [
        'Uni.form.field.Duration',
        'Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryGridActionMenu'
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
                xtype: 'mdc-device-connection-history-grid-action-menu'
            }
        }
    ],

    items: [

        {
            xtype: 'form',
            border: false,
            width: '100%',
            itemId: 'deviceConnectionHistoryPreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'vbox'
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            htmlEncode: false,
                            name: 'connectionSummary',
                            fieldLabel: Uni.I18n.translate('deviceconnectionhistory.connectionSummary', 'MDC', 'Connection summary'),
                            itemId: 'connection-summary',
                            labelWidth: 250
                        },
                        {
                            xtype: 'button',
                            itemId: 'btn-show-connection-details',
                            text: Uni.I18n.translate('deviceconnectionhistory.showConnectionDetails','MDC','Show connection details'),
                            action: 'showConnectionDetails',
                            margin: '0 0 0 10'
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
                                columnWidth: 0.49,
                                itemId: 'right-connection-details',
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
                                        name: 'startedOn',
                                        fieldLabel: Uni.I18n.translate('deviceconnectionhistory.startedOn', 'MDC', 'Started on'),
                                        itemId: 'startedOn',
                                        renderer: function (value) {
                                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'finishedOn',
                                        fieldLabel: Uni.I18n.translate('deviceconnectionhistory.finishedOn', 'MDC', 'Finished on'),
                                        itemId: 'finishedOn',
                                        renderer: function (value) {
                                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                        }
                                    },
                                    {
                                        xtype: 'uni-form-field-duration',
                                        name: 'durationInSeconds',
                                        fieldLabel: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                                        itemId: 'durationInSeconds',
                                        usesSeconds: true

                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'connectionType',
                                        fieldLabel: Uni.I18n.translate('deviceconnectionhistory.connectionType', 'MDC', 'Connection type'),
                                        itemId: 'connectionType'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'direction',
                                        fieldLabel: Uni.I18n.translate('deviceconnectionhistory.direction', 'MDC', 'Direction'),
                                        itemId: 'direction'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('deviceconnectionhistory.comPort', 'MDC', 'Communication port'),
                                        itemId: 'comPort',
                                        htmlEncode: false
                                    }


                                ]
                            },
                                {
                                    xtype: 'container',
                                    columnWidth: 0.5,
                                    itemId: 'left-connection-details',
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
                                            name: 'status',
                                            fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                            itemId: 'statusLink',
                                            htmlEncode: false
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'result',
                                            fieldLabel: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                                            itemId: 'result',
                                            renderer: function (value) {
                                                if (value) {
                                                    return Ext.String.htmlEncode(value.displayValue);
                                                }
                                            }
                                        },
                                        {
                                            xtype: 'displayfield',
                                            fieldLabel: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                                            name: 'comTaskCount',
                                            cls: 'communication-tasks-status',
                                            renderer: function (val) {
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

                                                if(val){
                                                    if (this.tooltip) {
                                                        this.tooltip.update(tooltipText);
                                                    } else {
                                                        this.tooltip = Ext.create('Ext.tip.ToolTip', {
                                                            target: this.getEl(),
                                                            html: tooltipText
                                                        });
                                                    }
                                                }

                                                template += '<tpl><span class="icon-checkmark"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</tpl>';
                                                template += '<tpl><span class="icon-cross"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</tpl>';
                                                template += '<tpl><span  class="icon-stop2"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</tpl>';
                                                return template;
                                            }
                                        }
                                    ]
                                }]
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




