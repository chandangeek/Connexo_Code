/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.view.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycle-state-edit',
    itemId: 'dlc-state-edit',
    requires: [
        'Uni.view.container.ContentContainer',
        'Uni.grid.column.Default',
        'Uni.grid.column.RemoveAction',
        'Dlc.devicelifecyclestates.store.Stages'
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
                    enforceMaxLength: true,
                    listeners: {
                        afterrender: function (field) {
                            field.focus(false, 200);
                        }
                    }
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
                    xtype: 'combobox',
                    itemId: 'cbo-stage',
                    name: 'stageName',
                    msgTarget: 'under',
                    required: true,
                    fieldLabel: Uni.I18n.translate('general.Stage', 'DLC', 'Stage'),
                    store: 'Dlc.devicelifecyclestates.store.Stages',
                    forceSelection: true,
                    emptyText: Uni.I18n.translate('deviceLifeCycleStates.selectAStage', 'DLC', 'Select a stage...'),
                    displayField: 'displayValue',
                    valueField: 'id'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('transitionBusinessProcess.entry', 'DLC', 'Processes on entry'),
                    itemId: 'processesOnEntryContainer',
                    required: false,
                    msgTarget: 'under',
                    layout: 'hbox',
                    width: 1200,
                    items: [
                        {
                            xtype: 'gridpanel',
                            itemId: 'processesOnEntryGrid',
                            store: 'Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnEntry',
                            hideHeaders: true,
                            disableSelection: true,
                            trackMouseOver: false,
                            width: 600,
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
                                        this.up('#dlc-state-edit').showGridsOrMessages();
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
                            html: Uni.I18n.translate('deviceLifeCycleStates.noProcessesAdded', 'DLC', 'No processes have been added'),
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
                            action:'onEntry',
                            text: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'DLC', 'Add processes'),
                            margin: '0 0 0 10'
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('transitionBusinessProcess.exit', 'DLC', 'Processes on exit'),
                    itemId: 'processesOnExitContainer',
                    required: false,
                    msgTarget: 'under',
                    layout: 'hbox',
                    width: 1200,
                    items: [
                        {
                            xtype: 'component',
                            html: Uni.I18n.translate('deviceLifeCycleStates.noProcessesAdded', 'DLC', 'No processes have been added'),
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
                            store: 'Dlc.devicelifecyclestates.store.TransitionBusinessProcessesOnExit',
                            hideHeaders: true,
                            disableSelection: true,
                            trackMouseOver: false,
                            width: 600,
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
                                        this.up('#dlc-state-edit').showGridsOrMessages();
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
                            action:'onExit',
                            text: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'DLC', 'Add processes'),
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
            ],

            loadRecord: function (record) {
                if (Ext.isEmpty(record.get('id'))) { //Add
                    this.setTitle(Uni.I18n.translate('deviceLifeCycleStates.add', 'DLC', 'Add state'));
                }else{ //Edit
                    var createBtn = this.down('#createEditButton');

                    this.setTitle(Uni.I18n.translate('general.editx', 'DLC', "Edit '{0}'", record.get('name')));
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
                var processOnEntryStore = this.down('#processesOnEntryGrid').getStore();
                if (processOnEntryStore.modelId != record.getId()){
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
                this.up('#dlc-state-edit').showGridsOrMessages();
            }

        }
    ],

    showGridsOrMessages : function() {
        var processOnEntryStore = this.down('#processesOnEntryGrid').getStore(),
            processOnExitStore = this.down('#processesOnExitGrid').getStore();

        if (processOnEntryStore.getCount() > 0) {
            this.down('#noOnEntryProcessesAddedMsg').hide();
            this.down('#processesOnEntryGrid').show();
        } else {
            this.down('#noOnEntryProcessesAddedMsg').show();
            this.down('#processesOnEntryGrid').hide();
        }
        if (processOnExitStore.getCount() > 0) {
            this.down('#noOnExitProcessesAddedMsg').hide();
            this.down('#processesOnExitGrid').show();
        } else {
            this.down('#noOnExitProcessesAddedMsg').show();
            this.down('#processesOnExitGrid').hide();
        }
    }
});
