Ext.define('Dlc.devicelifecyclestates.view.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycle-state-edit',
    requires: [
        'Uni.view.container.ContentContainer',
        'Uni.grid.column.Default'
    ],

    content: [
        {
            xtype: 'form',
            itemId: 'lifeCycleStateEditForm',
            ui: 'large',
            defaults: {
                labelWidth: 200,
                validateOnChange: false,
                validateOnBlur: false,
                width: 500
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    msgTarget: 'under',
                    required: true,
                    fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                    itemId: 'lifeCycleStateNameField',
                    maxLength: 80,
                    enforceMaxLength: true
                },
                {
                    xtype: 'displayfield',
                    name: 'sorted_name',
                    itemId: 'lifeCycleStateNameDisplayField',
                    fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                    required: true,
                    hidden: true
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('transitionBusinessProcess.entry', 'DLC', 'Processes on entry'),
                    hidden: false,
                    itemId: 'processesOnEntryContainer',
                    name: 'onEntry',
                    required: false,
                    msgTarget: 'under',
                    width: 1200,
                    items: [
                        {
                            xtype: 'gridpanel',
                            width: 800,
                            height: 220,
                            itemId: 'processesOnEntryGridPanel',
                            store: 'Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnEntry',
                            hideHeaders: true,
                            padding: 0,
                            columns: [
                                {
                                    dataIndex: 'processId',
                                    flex: 1
                                },
                                {
                                    xtype: 'actioncolumn',
                                    align: 'right',
                                    items: [
                                        {
                                            iconCls: 'uni-icon-delete',
                                            handler: function (grid, rowIndex) {
                                                grid.getStore().removeAt(rowIndex);
                                            }
                                        }
                                    ]
                                }
                            ],
                            rbar: [
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'button',
                                            itemId: 'addOnEntryTransitionBusinessProcess',
                                            text: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'DLC', 'Add processes'),
                                            action:'onEntry',
                                            margin: '0 0 0 10'
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('transitionBusinessProcess.exit', 'DLC', 'Processes on exit'),
                    hidden: false,
                    itemId: 'processesOnExitContainer',
                    name: 'onExit',
                    required: false,
                    msgTarget: 'under',
                    width: 1200,
                    items: [
                        {
                            xtype: 'gridpanel',
                            width: 800,
                            height: 220,
                            store: 'Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnExit',
                            itemId: 'processesOnExitGridPanel',
                            hideHeaders: true,
                            padding: 0,
                            columns: [
                                {
                                    dataIndex: 'processId',
                                    flex: 1
                                },
                                {
                                    xtype: 'actioncolumn',
                                    align: 'right',
                                    items: [
                                        {
                                            iconCls: 'uni-icon-delete',
                                            handler: function (grid, rowIndex) {
                                                grid.getStore().removeAt(rowIndex);
                                            }
                                        }
                                    ]
                                }
                            ],
                            rbar: [
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'button',
                                            itemId: 'addOnExitTransitionBusinessProcess',
                                            text: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'DLC', 'Add processes'),
                                            action:'onExit',
                                            margin: '0 0 0 10'
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'DLC', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'add',
                            itemId: 'createEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'DLC', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            action: 'cancelAction'
                        }
                    ]
                }
            ] ,
            loadRecord: function (record) {
                if (Ext.isEmpty(record.get('id'))) { //Add
                    this.setTitle(Uni.I18n.translate('deviceLifeCycleStates.add', 'DLC', 'Add state'));
                }else{ //Edit
                    var title = Uni.I18n.translate('general.edit', 'DLC', 'Edit') + " '" + record.get('name') + "'"
                    this.setTitle(title);

                    var createBtn = this.down('#createEditButton');
                    createBtn.setText(Uni.I18n.translate('general.save', 'DLC', 'Save'));
                    createBtn.action = 'save';
                    if (!record.get('isCustom')) {
                        this.getForm().findField('name').disabled = true;
                        this.down('#lifeCycleStateNameField').hide();
                        var displayNameField  = this.down('#lifeCycleStateNameDisplayField');
                        displayNameField.setValue(record.get('name'));
                        displayNameField.show();
                    }
                }
                this.getForm().loadRecord(record);
                var processOnEntryStore = this.down('#processesOnEntryGridPanel').getStore();
                if (processOnEntryStore.modelId != record.getId()){
                    processOnEntryStore.removeAll();
                    Ext.each(record.get("onEntry"), function (transitionBusinessProcess) {
                        processOnEntryStore.add(transitionBusinessProcess);
                    });
                    processOnEntryStore.modelId = record.getId();
                }
                var processOnExitStore = this.down('#processesOnExitGridPanel').getStore();
                if (processOnExitStore.modelId != record.getId()) {
                    processOnExitStore.removeAll();
                    Ext.each(record.get("onExit"), function (transitionBusinessProcess) {
                        processOnExitStore.add(transitionBusinessProcess);
                    });
                    processOnExitStore.modelId = record.getId();
                }
            }

        }
    ]
});
