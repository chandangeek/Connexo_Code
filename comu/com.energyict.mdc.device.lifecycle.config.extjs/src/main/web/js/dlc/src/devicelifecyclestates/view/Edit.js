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
                    name: 'stage',
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
                            width: 285,
                            maxHeight: 300,
                            hidden: true,
                            style: {
                                paddingBottom: '8px'
                            },
                            columns: [
                                {
                                    dataIndex: 'name',
                                    renderer: function (value, metaData, record) {
                                        return value + ' (' + record.get('version') + ')';
                                    },
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
                            width: 285,
                            maxHeight: 300,
                            hidden: true,
                            style: {
                                paddingBottom: '8px'
                            },
                            columns: [
                                {
                                    dataIndex: 'name',
                                    renderer: function (value, metaData, record) {
                                        return value + ' (' + record.get('version') + ')';
                                    },
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
                    fieldLabel: Uni.I18n.translate('deviceLifeCycleStates.webServiceCall.entry', 'DLC', 'Web service call on entry'),
                    itemId: 'webServicesOnEntryContainer',
                    required: false,
                    msgTarget: 'under',
                    layout: 'hbox',
                    width: 1200,
                    items: [
                        {
                            xtype: 'gridpanel',
                            itemId: 'webServicesOnEntryGrid',
                            store: 'Dlc.devicelifecyclestates.store.WebServiceEndpointsOnEntry',
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
                            html: Uni.I18n.translate('deviceLifeCycleStates.noWebServicesAdded', 'DLC', 'No web service endpoints have been added'),
                            itemId: 'noOnEntryWebServicesAddedMsg',
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
                            itemId: 'addOnEntryWebServiceEndpoints',
                            action:'onEntry',
                            text: Uni.I18n.translate('transitionBusinessProcess.addWebServiceEndpoint', 'DLC', 'Add web service endpoint'),
                            margin: '0 0 0 10'
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('deviceLifeCycleStates.webServiceCall.exit', 'DLC', 'Web service call on exit'),
                    itemId: 'webServicesOnExitContainer',
                    required: false,
                    msgTarget: 'under',
                    layout: 'hbox',
                    width: 1200,
                    items: [
                        {
                            xtype: 'component',
                            html: Uni.I18n.translate('deviceLifeCycleStates.noWebServicesAdded', 'DLC', 'No web service endpoints have been added'),
                            itemId: 'noOnExitWebServicesAddedMsg',
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
                            itemId: 'webServicesOnExitGrid',
                            store: 'Dlc.devicelifecyclestates.store.WebServiceEndpointsOnExit',
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
                            itemId: 'addOnExitWebServiceEndpoints',
                            action:'onExit',
                            text: Uni.I18n.translate('transitionBusinessProcess.addWebServiceEndpoint', 'DLC', 'Add web service endpoint'),
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
                if (record.get('stage') && !this.down('#cbo-stage').getValue()) {
                    this.down('#cbo-stage').select(record.get('stage').id);
                }
                var processOnEntryStore = this.down('#processesOnEntryGrid').getStore();
                if (processOnEntryStore.modelId !== record.getId()){
                    processOnEntryStore.removeAll();
                    Ext.each(record.get("onEntry"), function (transitionBusinessProcess) {
                        processOnEntryStore.add(transitionBusinessProcess);
                    });
                    processOnEntryStore.modelId = record.getId();
                }

                var processOnExitStore = this.down('#processesOnExitGrid').getStore();
                if (processOnExitStore.modelId !== record.getId()) {
                    processOnExitStore.removeAll();
                    Ext.each(record.get("onExit"), function (transitionBusinessProcess) {
                        processOnExitStore.add(transitionBusinessProcess);
                    });
                    processOnExitStore.modelId = record.getId();
                }
                var webServicesOnEntryStore = this.down('#webServicesOnEntryGrid').getStore();
                if (webServicesOnEntryStore.modelId !== record.getId()){
                    webServicesOnEntryStore.removeAll();
                    Ext.each(record.get("onEntryEndPointConfigurations"), function (webServices) {
                        webServicesOnEntryStore.add(webServices);
                    });
                    webServicesOnEntryStore.modelId = record.getId();
                }

                var webServicesOnExitStore = this.down('#webServicesOnExitGrid').getStore();
                if (webServicesOnExitStore.modelId !== record.getId()) {
                    webServicesOnExitStore.removeAll();
                    Ext.each(record.get("onExitEndPointConfigurations"), function (webServices) {
                        webServicesOnExitStore.add(webServices);
                    });
                    webServicesOnExitStore.modelId = record.getId();
                }
                this.up('#dlc-state-edit').showGridsOrMessages();
            }

        }
    ],

    showGridsOrMessages : function() {
        var processOnEntryStore = this.down('#processesOnEntryGrid').getStore(),
            processOnExitStore = this.down('#processesOnExitGrid').getStore(),
            webServiceOnEntryStore = this.down('#webServicesOnEntryGrid').getStore(),
            webServiceOnExitStore = this.down('#webServicesOnExitGrid').getStore();

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
        if (webServiceOnEntryStore.getCount() > 0) {
            this.down('#noOnEntryWebServicesAddedMsg').hide();
            this.down('#webServicesOnEntryGrid').show();
        } else {
            this.down('#noOnEntryWebServicesAddedMsg').show();
            this.down('#webServicesOnEntryGrid').hide();
        }
        if (webServiceOnExitStore.getCount() > 0) {
            this.down('#noOnExitWebServicesAddedMsg').hide();
            this.down('#webServicesOnExitGrid').show();
        } else {
            this.down('#noOnExitWebServicesAddedMsg').show();
            this.down('#webServicesOnExitGrid').hide();
        }
    }
});
