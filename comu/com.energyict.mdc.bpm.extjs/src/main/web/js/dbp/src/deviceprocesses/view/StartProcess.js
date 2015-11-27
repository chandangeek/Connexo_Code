Ext.define('Dbp.deviceprocesses.view.StartProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-start-processes',
    store: 'Dbp.deviceprocesses.store.HistoryProcessesFilterProcesses',
    requires: [
        'Dbp.deviceprocesses.view.RunningProcessPreview',
        'Dbp.deviceprocesses.view.RunningProcessesGrid'
    ],

    router: null,
    device: null,
    initComponent: function () {
        var me = this,
            processStore = Ext.getStore('Dbp.deviceprocesses.store.HistoryProcessesFilterProcesses'),
            processCombo;
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
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('processes.start.title', 'DBP', 'Start process'),
                itemId: 'pnl-processes',
                items: [
                    {
                        xtype: 'form',
                        itemId: 'frm-process-start',
                        margin: '0 0 0 0',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        items: [
                            {
                                xtype: 'combobox',
                                dataIndex: 'name',
                                fieldLabel: Uni.I18n.translate('dbp.process.start.process', 'DBP', 'Process'),
                                emptyText: Uni.I18n.translate('dbp.process.startTyping', 'DBP', 'Start typing for process...'),
                                multiSelect: false,
                                displayField: 'name',
                                valueField: 'id',
                                itemId: 'cbo-processes-definition',
                                store: 'Dbp.deviceprocesses.store.HistoryProcessesFilterProcesses',
                                width: 600,
                                labelWidth: 250,
                                queryMode: 'local',
                                name: 'startProcessCombo',
                                required: true
                            }
                        ]
                    }
                                    ]

            }
        ];
        me.callParent(arguments);
        processCombo = me.down('combobox[name=startProcessCombo]');
        processStore.load(function (records) {
            Ext.getBody().unmask();
            if (!Ext.isEmpty(records)) {
                processCombo.bindStore(processStore);
            }
        });
    }
});

