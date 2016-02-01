Ext.define('Bpm.startprocess.view.StartProcess', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bpm-start-processes-panel',

    items: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('bpm.startProcess.title', 'BPM', 'Start process'),
            itemId: 'processes-panel',
            items: [
                {
                    xtype: 'form',
                    itemId: 'start-process-form',
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
                            fieldLabel: Uni.I18n.translate('bpm.startProcess.process', 'BPM', 'Process'),
                            emptyText: Uni.I18n.translate('bpm.startProcess.startTyping', 'BPM', 'Start typing for process...'),
                            multiSelect: false,
                            displayField: 'displayname',
                            valueField: 'id',
                            itemId: 'processes-definition-combo',
                            allowBlank: false,
                            width: 500,
                            labelWidth: 150,
                            queryMode: 'local',
                            name: 'startProcessCombo',
                            required: true,
                            editable: false
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
                                            text: Uni.I18n.translate('bpm.startProcess.action', 'BPM', 'Start'),
                                            xtype: 'button',
                                            ui: 'action',
                                            itemId: 'start-button',
                                            action: 'startProcess'

                                        },
                                        {
                                            xtype: 'button',
                                            text: Uni.I18n.translate('general.cancel', 'BPM', 'Cancel'),
                                            itemId: 'cancel-link-button',
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
            itemId: 'no-items-found-panel',
            margin: '15 0 20 0',
            hidden: true,
            title: Uni.I18n.translate('dbp.process.start.title', 'BPM', 'Start process'),
            reasons: [
                Uni.I18n.translate('bpm.startProcess.empty.list.item1', 'BPM', 'No processes have been defined yet.'),
                Uni.I18n.translate('bpm.startProcess.empty.list.item2', 'BPM', 'No processes are available for the current device state.'),
                Uni.I18n.translate('bpm.startProcesss.empty.list.item3', 'BPM', 'Processes exist, but you do not have permission to execute them.')
            ]
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.properties.processesStore.load(function (records) {
            var visible = Ext.isEmpty(records);

            Ext.getBody().unmask();
            me.down('#processes-panel').setVisible(!visible);
            me.down('#no-items-found-panel').setVisible(visible);
            if (!visible) {
                me.down('combobox[name=startProcessCombo]').bindStore(me.properties.processesStore);
            }
        });

    }
});

