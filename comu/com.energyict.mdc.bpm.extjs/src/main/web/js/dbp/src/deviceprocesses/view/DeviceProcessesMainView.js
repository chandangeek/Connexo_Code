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
                        margin: '0 0 0 -15',
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
                        margin: '0 0 0 -15',
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

            }
        ];
        me.callParent(arguments);
    }
});