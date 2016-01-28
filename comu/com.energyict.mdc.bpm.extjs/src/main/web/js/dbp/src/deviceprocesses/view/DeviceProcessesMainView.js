Ext.define('Dbp.deviceprocesses.view.DeviceProcessesMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-device-processes-main-view',
    requires: [
        'Dbp.deviceprocesses.view.RunningProcesses',
        'Dbp.deviceprocesses.view.HistoryProcesses',
        'Dbp.deviceprocesses.view.RunningProcessPreview',
        'Dbp.deviceprocesses.view.HistoryProcessPreview',
        'Dbp.deviceprocesses.view.StatusProcessPreview',
    ],
    device: null,
    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        ui: 'medium',
                        items: [
                            {
                                xtype: 'deviceMenu',
                                itemId: 'steps-Menu',
                                device: me.device,
                                toggleId: 'processesLink'
                            }
                        ]
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                title: Uni.I18n.translate('processes.title', 'DBP', 'Processes'),
                itemId: 'tab-processes',
                activeTab: -1,
                items: [
                    {
                    //    ui: 'medium',
                        title: Uni.I18n.translate('processes.processesRunning.title', 'DBP', 'Running processes'),
                        itemId: 'running-processes-tab',
                        items: [
                            {
                                xtype: 'dbp-running-processes',
                                itemId: 'running-processes'
                            }
                        ]
                    },
                    {
                    //    ui: 'medium',
                        title: Uni.I18n.translate('processes.processesHistory.title', 'DBP', 'History'),
                        itemId: 'history-processes-tab',
                        items: [
                            {
                                xtype: 'dbp-history-processes',
                                itemId: 'history-processes'
                            }
                        ]
                    }
                ]

            },
            {
                xtype: 'tabpanel',
                deferredRender : false,
                ui: 'large',
                itemId: 'tab-process-preview',
                activeTab: 0,
                items: [
                    {
                        ui: 'medium',
                        title: Uni.I18n.translate('processes.processDetails.title', 'DBP', 'Details'),
                        itemId: 'details-process-tab',
                        items: [
                            {
                                xtype: 'dbp-running-process-preview',
                                itemId: 'running-process-preview'
                            },
                            {
                                xtype: 'dbp-history-process-preview',
                                itemId: 'history-process-preview'
                            }
                        ]
                    },
                    {
                        ui: 'medium',
                        title: Uni.I18n.translate('processes.processStatus.title', 'DBP', 'Status overview'),
                        itemId: 'status-process-tab',
                        items: [
                            {
                                xtype: 'dbp-status-process-preview',
                                itemId: 'status-process-preview'
                            }
                        ]
                    }
                ]

            }
        ];
        me.callParent(arguments);
    }
});