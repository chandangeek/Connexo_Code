/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.startprocess.view.StartProcess', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bpm-start-processes-panel',
    requires: [
        'Bpm.startprocess.store.AvailableProcesses',
        'Uni.util.FormErrorMessage'
    ],
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
                    layout: {
                        type: 'vbox',
                        align: 'left'
                    },
                    defaults: {
                        labelWidth: 150,
                        width: 500
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            margin: '0 0 10 0',
                            hidden: true
                        },
                        {
                            xtype: 'combobox',
                            dataIndex: 'displayname',
                            fieldLabel: Uni.I18n.translate('bpm.startProcess.process', 'BPM', 'Process'),
                            emptyText: Uni.I18n.translate('bpm.startProcess.selectProcess', 'BPM', 'Select a process'),
                            multiSelect: false,
                            displayField: 'displayname',
                            valueField: 'processId',
                            itemId: 'processes-definition-combo',
                            allowBlank: false,
                            queryMode: 'local',
                            name: 'startProcessCombo',
                            required: true,
                            editable: false
                        }
                    ]
                },
                {
                    xtype: 'form',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    privileges: Bpm.privileges.BpmManagement.execute,
                    itemId: 'process-start-form',
                    items: [
                        {
                            xtype: 'container',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            itemId: 'process-start-content',
                            items: [
                                {
                                    xtype: 'property-form',
                                    defaults: {
                                        labelWidth: 150,
                                        width: 335
                                    }
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
            title: Uni.I18n.translate('bpm.process.start.title', 'BPM', 'Start process'),
            defaultReasons: [
                Uni.I18n.translate('bpm.startProcess.empty.list.item1', 'BPM', 'No processes have been defined yet.'),
                Uni.I18n.translate('bpm.startProcess.empty.list.item2', 'BPM', 'Processes exist, but you do not have permission to execute them.')
            ],
            reasons: []
        }
    ],

    initComponent: function () {
        var me = this,
            availableStore;

        if (Array.isArray(me.properties.additionalReasons)) {
            me.items[1].reasons = me.items[1].defaultReasons.concat(me.properties.additionalReasons);
        }
        else {
            me.items[1].reasons = me.items[1].defaultReasons;
        }

        me.callParent(arguments);

        availableStore = Ext.create('Bpm.startprocess.store.AvailableProcesses');
        availableStore.getProxy().extraParams = me.properties.activeProcessesParams;
        var viewport = Ext.ComponentQuery.query('viewport')[0];
        viewport.setLoading();
        availableStore.load(function (records) {
            var visible = Ext.isEmpty(records);

            Ext.getBody().unmask();
            me.down('#processes-panel').setVisible(!visible);
            me.down('#no-items-found-panel').setVisible(visible);
            if (!visible) {
                me.down('combobox[name=startProcessCombo]').bindStore(availableStore);
            }
            viewport.setLoading(false);
        });
        if (me.properties.context) {
            me.down('property-form').context = me.properties.context;
        }

    }
});

