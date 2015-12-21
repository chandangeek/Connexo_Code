Ext.define('Dbp.deviceprocesses.view.StartProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-start-processes',
    store: 'Dbp.deviceprocesses.store.HistoryProcessesFilterProcesses',
    requires: [
        'Dbp.deviceprocesses.view.RunningProcessPreview',
        'Dbp.deviceprocesses.view.RunningProcessesGrid'
    ],
    startProcessRecord: null,
    router: null,
    device: null,
    initComponent: function () {
        var me = this,
            processStore = Ext.getStore('Dbp.deviceprocesses.store.AvailableProcesses'),
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
                title: Uni.I18n.translate('dbp.process.start.title', 'DBP', 'Start process'),
                itemId: 'pnl-processes',
                items: [
                    {
                        xtype: 'form',
                        itemId: 'frm-process-start',
                        margin: '0 50 10 0',
                        layout: {
                            type: 'vbox',
                            align: 'left'
                        },
                        items: [
                            {
                                itemId: 'form-errors',
                                xtype: 'uni-form-error-message',
                                name: 'form-errors',
                                width: 600,
                                margin: '0 0 10 0',
                                hidden: true
                            },
                            {
                                xtype: 'combobox',
                                dataIndex: 'displayname',
                                fieldLabel: Uni.I18n.translate('dbp.process.start.process', 'DBP', 'Process'),
                                emptyText: Uni.I18n.translate('dbp.process.startTyping', 'DBP', 'Start typing for process...'),
                                multiSelect: false,
                                displayField: 'displayname',
                                valueField: 'id',
                                itemId: 'cbo-processes-definition',
                                allowBlank: false,
                                width: 500,
                                labelWidth: 150,
                                queryMode: 'local',
                                name: 'startProcessCombo',
                                required: true
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        ui: 'medium',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        privileges: Bpm.privileges.BpmManagement.execute,
                        itemId: 'process-start-form',
                        items: [
                            {
                                xtype: 'container',
                                margin: '20 0 0 0',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                itemId: 'process-start-content',
                                items: [
                                    {
                                        xtype: 'property-form'
                                    },
                                    {
                                        xtype: 'container',
                                        margin: '10 0 0 165',
                                        layout: 'hbox',
                                        items: [
                                            {
                                                text: Uni.I18n.translate('dbp.process.action', 'DBP', 'Start'),
                                                xtype: 'button',
                                                ui: 'action',
                                                itemId: 'btn-start',
                                                action: 'startProcess',
                                                startProcessRecord: me.startProcessRecord

                                            },
                                            {
                                                xtype: 'button',
                                                text: Uni.I18n.translate('general.cancel', 'DBP', 'Cancel'),
                                                itemId: 'btn-cancel-link',
                                                action: 'cancelStartProcess',
                                                ui: 'link'
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]

            },
            {
                xtype: 'no-items-found-panel',
                itemId: 'pnl-no-items-found',
                margin: '15 0 20 0',
                hidden: true,
                title: Uni.I18n.translate('dbp.process.start.title', 'DBP', 'Start process'),
                reasons: [
                    Uni.I18n.translate('dbp.startprocess.empty.list.item1', 'DBP', 'No processes have been defined yet.'),
                    Uni.I18n.translate('dbp.startprocess.empty.list.item2', 'DBP', 'No processes are available for the current device state.'),
                    Uni.I18n.translate('dbp.startprocess.empty.list.item3', 'DBP', 'Processes exist, but you do not have permission to execute them.')
                ]
            }
        ];
        me.callParent(arguments);
        processCombo = me.down('combobox[name=startProcessCombo]');

        processStore.load(function (records) {
            var visible = Ext.isEmpty(records);
            Ext.getBody().unmask();
            me.down('#pnl-processes').setVisible(!visible);
            me.down('#pnl-no-items-found').setVisible(visible);
            if (!visible) {
                processCombo.bindStore(processStore);
            }
        });
    }
});

