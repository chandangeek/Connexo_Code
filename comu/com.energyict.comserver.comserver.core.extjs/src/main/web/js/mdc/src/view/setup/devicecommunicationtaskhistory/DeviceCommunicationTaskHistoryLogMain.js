Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryLogMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationTaskHistoryLogMain',
    itemId: 'deviceCommunicationTaskHistoryLogMain',

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        mRID: this.mRID,
                        toggle: 6
                    },
                    {
                        xtype: 'deviceCommunicationTaskHistorySideFilter'
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('devicecommunicationtaskhistory.communicationLog', 'MDC', 'Communication log'),
                items: [
                    {
                        xtype: 'form',
                        border: true,
                        itemId: 'deviceCommunicationTaskLogOverviewForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'column'
//                        align: 'stretch'
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
                                                name: 'startTime',
                                                fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.startedOn', 'MDC', 'Started on'),
                                                itemId: 'startTime',
                                                renderer: function (value) {
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
                                                itemId: 'duration',
                                                renderer: function (value) {
                                                    if (value !== '') {
                                                        return value + ' ' + Uni.I18n.translate('general.seconds', 'MDC', 'seconds');
                                                    } else {
                                                        return '';
                                                    }
                                                }
                                            },
                                            {
                                                xtype: 'displayfield',
                                                name: 'result',
                                                fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.result', 'MDC', 'Result')
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
                                                fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.connectionUsed', 'MDC', 'Connection used'),
                                                name: 'comSession',
                                                renderer: function(value){
                                                    if(value){
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
                        xtype: 'filter-top-panel',
                        itemId: 'deviceCommunicationTaskLogFilterTopPanel'
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceCommunicationTaskHistoryLogGrid',
                            mrid: this.mrid
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('deviceconnectionhistoryLog.empty.title', 'MDC', 'No logs found'),
                            reasons: [
                                Uni.I18n.translate('deviceconnectionhistoryLog.empty.list.item1', 'MDC', 'The communication failed before communication logs could be created'),
                                Uni.I18n.translate('deviceconnectionhistoryLog.empty.list.item2', 'MDC', 'The filter is too narrow')
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceCommunicationTaskHistoryLogPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});