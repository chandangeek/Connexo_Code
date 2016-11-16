Ext.define('Imt.usagepointlifecyclestates.view.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycle-state-edit',
    requires: [
        'Uni.view.container.ContentContainer',
        'Uni.grid.column.Default',
        'Uni.grid.column.RemoveAction'
    ],

    content: [
        {
            xtype: 'form',
            itemId: 'lifeCycleStateEditForm',
            ui: 'large',
            defaults: {
                labelWidth: 200,
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
                    fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                    itemId: 'lifeCycleStateNameField',
                    maxLength: 80,
                    enforceMaxLength: true,
                    listeners: {
                        afterrender: function (field) {
                            field.focus(false);
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'display_name',
                    itemId: 'lifeCycleStateNameDisplayField',
                    fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                    required: true,
                    hidden: true
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('transitionBusinessProcess.entry', 'IMT', 'Processes on entry'),
                    itemId: 'processesOnEntryContainer',
                    required: false,
                    msgTarget: 'under',
                    layout: 'hbox',
                    width: 1200,
                    items: [
                        {
                            xtype: 'gridpanel',
                            itemId: 'processesOnEntryGrid',
                            store: 'Imt.usagepointlifecyclestates.store.TransitionBusinessProcessesOnEntry',
                            hideHeaders: true,
                            disableSelection: true,
                            trackMouseOver: false,
                            width: 285,
                            maxHeight: 300,
                            hidden: true,
                            style: {
                                paddingBottom: '8px'
                            },
                            columns: [
                                {
                                    dataIndex: 'name',
                                    flex: 1
                                },
                                {
                                    xtype: 'uni-actioncolumn-remove',
                                    align: 'right',
                                    handler: function (grid, rowIndex) {
                                        grid.getStore().removeAt(rowIndex);
                                        this.up('usagepoint-life-cycle-state-edit').showGridsOrMessages();
                                    }
                                }
                            ],
                            listeners: {
                                afterrender: function () {
                                    this.view.el.dom.style.overflowX = 'hidden'
                                }
                            }
                        },
                        {
                            xtype: 'component',
                            html: Uni.I18n.translate('usagePointLifeCycleStates.noProcessesAdded', 'IMT', 'No processes have been added'),
                            itemId: 'noOnEntryProcessesAddedMsg',
                            style: {
                                'font': 'italic 13px/17px Lato',
                                'color': '#686868',
                                'margin-top': '6px',
                                'margin-right': '10px'
                            },
                            hidden: true
                        },
                        {
                            xtype: 'button',
                            itemId: 'addOnEntryTransitionBusinessProcess',
                            action: 'onEntry',
                            text: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'IMT', 'Add processes'),
                            margin: '0 0 0 10'
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('transitionBusinessProcess.exit', 'IMT', 'Processes on exit'),
                    itemId: 'processesOnExitContainer',
                    required: false,
                    msgTarget: 'under',
                    layout: 'hbox',
                    width: 1200,
                    items: [
                        {
                            xtype: 'component',
                            html: Uni.I18n.translate('usagePointLifeCycleStates.noProcessesAdded', 'IMT', 'No processes have been added'),
                            itemId: 'noOnExitProcessesAddedMsg',
                            style: {
                                'font': 'italic 13px/17px Lato',
                                'color': '#686868',
                                'margin-top': '6px',
                                'margin-right': '10px'
                            },
                            hidden: true
                        },
                        {
                            xtype: 'grid',
                            itemId: 'processesOnExitGrid',
                            store: 'Imt.usagepointlifecyclestates.store.TransitionBusinessProcessesOnExit',
                            hideHeaders: true,
                            disableSelection: true,
                            trackMouseOver: false,
                            width: 285,
                            maxHeight: 300,
                            hidden: true,
                            style: {
                                paddingBottom: '8px'
                            },
                            columns: [
                                {
                                    dataIndex: 'name',
                                    flex: 1
                                },
                                {
                                    xtype: 'uni-actioncolumn-remove',
                                    align: 'right',
                                    handler: function (grid, rowIndex) {
                                        grid.getStore().removeAt(rowIndex);
                                        this.up('usagepoint-life-cycle-state-edit').showGridsOrMessages();
                                    }
                                }
                            ],
                            listeners: {
                                afterrender: function () {
                                    this.view.el.dom.style.overflowX = 'hidden'
                                }
                            }
                        },
                        {
                            xtype: 'button',
                            itemId: 'addOnExitTransitionBusinessProcess',
                            action: 'onExit',
                            text: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'IMT', 'Add processes'),
                            margin: '0 0 0 10'
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'add',
                            itemId: 'createEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            action: 'cancelAction'
                        }
                    ]
                }
            ],

            loadRecord: function (record) {
                Ext.suspendLayouts();
                if (Ext.isEmpty(record.get('id'))) { //Add
                    this.setTitle(Uni.I18n.translate('usagePointLifeCycleStates.add', 'IMT', 'Add state'));
                } else { //Edit
                    var createBtn = this.down('#createEditButton');

                    this.setTitle(Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", record.get('name')));
                    createBtn.setText(Uni.I18n.translate('general.save', 'IMT', 'Save'));
                    createBtn.action = 'save';
                    if (!record.get('isCustom')) {
                        this.getForm().findField('name').disabled = true;
                        this.down('#lifeCycleStateNameField').hide();
                        var displayNameField = this.down('#lifeCycleStateNameDisplayField');
                        displayNameField.setValue(record.get('name'));
                        displayNameField.show();
                    }
                }
                this.getForm().loadRecord(record);
                var processOnEntryStore = this.down('#processesOnEntryGrid').getStore();
                if (processOnEntryStore.modelId != record.getId()) {
                    processOnEntryStore.removeAll();
                    Ext.each(record.get("onEntry"), function (transitionBusinessProcess) {
                        processOnEntryStore.add(transitionBusinessProcess);
                    });
                    processOnEntryStore.modelId = record.getId();
                }

                var processOnExitStore = this.down('#processesOnExitGrid').getStore();
                if (processOnExitStore.modelId != record.getId()) {
                    processOnExitStore.removeAll();
                    Ext.each(record.get("onExit"), function (transitionBusinessProcess) {
                        processOnExitStore.add(transitionBusinessProcess);
                    });
                    processOnExitStore.modelId = record.getId();
                }
                this.up('usagepoint-life-cycle-state-edit').showGridsOrMessages();
                Ext.resumeLayouts(true);
            }
        }
    ],

    showGridsOrMessages: function () {
        var processOnEntryStore = this.down('#processesOnEntryGrid').getStore(),
            processOnExitStore = this.down('#processesOnExitGrid').getStore();

        this.down('#noOnEntryProcessesAddedMsg').setVisible(!processOnEntryStore.getCount());
        this.down('#processesOnEntryGrid').setVisible(processOnEntryStore.getCount());
        this.down('#noOnExitProcessesAddedMsg').setVisible(!processOnExitStore.getCount());
        this.down('#processesOnExitGrid').setVisible(processOnExitStore.getCount());
    }
});
