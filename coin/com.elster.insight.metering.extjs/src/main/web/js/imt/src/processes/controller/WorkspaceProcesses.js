/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.controller.WorkspaceProcesses', {
    extend: 'Ext.app.Controller',
    
    models: [
        'Bpm.monitorprocesses.model.ExtendedProcessNodes',
        'Bpm.monitorprocesses.model.ParentProcess'
    ],
    
    requires: [
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.monitorprocesses.controller.MonitorProcesses',
        'Uni.property.controller.Registry',
        'Imt.processes.controller.ProcInsightGlobalVars'
    ],
    
    controllers: [
        'Bpm.startprocess.controller.StartProcess',
        'Bpm.monitorprocesses.controller.MonitorProcesses'
    ],
    
    stores: [
        'Imt.usagepointsetup.store.Devices',
        'Imt.usagepointmanagement.store.MeterActivations',
        'Imt.processes.store.PurposesWithValidationRuleSets',
        'Imt.processes.store.AvailableTransitions',
        'Imt.processes.store.InsightProcessesStore',
        'Imt.processes.store.InsightProcessesFilterStore',
        'Imt.processes.store.InsightProcessTypeStore',
        'Imt.processes.store.InsightProcessesStatusStore',
        'Imt.processes.store.ObjectStoreInsightExt'
    ],
    
    views: [
        'Imt.processes.view.AllProcessesInsight',
        'Imt.processes.view.InsightProcessesGrid',
        'Imt.processes.view.ProcessPreview',
        'Imt.processes.view.ProcessPreviewForm'
    ],
    
    refs: [
        {ref: 'processesGrid', selector: '#processesGrid'},
        {ref: 'processes', selector: 'all-flow-processes-insight'},
        {ref: 'processPreview', selector: '#processPreview'},
        {ref: 'processPreviewForm', selector: '#processPreviewForm'},
        {ref: 'openTasksDisplay', selector: '#processPreviewForm #preview-running-process-open-tasks-all-processes'},
        {ref: 'processStatusPreviewExtendedTab', selector: '#all-process-status-preview-extended'},
        {ref: 'processStatusPreviewExtendedGrid', selector: '#all-process-status-preview-extended #process-nodes-grid-extended'},
        {ref: 'statusVariablesPreviewExtendedPanel', selector: '#all-process-status-preview-extended #node-variables-preview-panel'},
        {ref: 'childProcessPreviewExtendedPanel', selector: '#all-process-status-preview-extended #child-process-preview-panel'},
		{ref: 'parentProcessPreviewExtendedPanel', selector: '#all-process-status-preview-extended #parent-process-preview-panel'}
    ],

	init: function () {
        this.control({
            '#processesGrid': {
               selectionchange: this.previewProcess
            },
            '#all-process-status-preview-extended #process-nodes-grid-extended': {
                select: this.showVariablesPreviewExtendedForStatus
            },
            'processes-sorting-menu': {
                click: this.chooseSort
            },
            '#processes-sorting-toolbar #itemsContainer button': {
                click: this.switchSortingOrder
            },
            '#processes-sorting-toolbar button[action=clear]': {
                click: this.clearAllSorting
            },
            '#processes-sorting-toolbar button': {
                closeclick: this.onSortCloseClicked
            }
        });
    },

	showAllProcessesInsight: function() {
	
		var me = this;
        var queryString = Uni.util.QueryString.getQueryStringValues(false);

        /* If queryString is empty and setDefaultParams is true it means that probably it is firs load of page.
        And we should set default params.*/
        if (_.isEmpty(queryString) && Imt.processes.controller.ProcInsightGlobalVars.setDefaultParams) {
            /*First load of page with processes*/
            queryString.status = ['1'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
            /* Set default values for sorting panel */
            me.setDefaultSort();
        }else{
			Imt.processes.controller.ProcInsightGlobalVars.setDefaultParams = true;
			var routerToSet = this.getController('Uni.controller.history.Router');
			var widget = Ext.widget('allProcessesInsight', {
				router: routerToSet
			});
			this.getApplication().fireEvent('changecontentevent', widget);
			me.updateSortingToolbar();
		}
	},
	
	setDefaultSort: function () {
        var me = this,
            store = me.getStore('Imt.processes.store.InsightProcessesStore'),
            sorting = store.getProxy().extraParams['sort'];

        if (sorting === undefined) { // set default filters
            sorting = [];
            sorting.push({
                property: 'processId',
                direction: Uni.component.sort.model.Sort.DESC
            });
            store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        }
    },
    
    updateSortingToolbar: function () {
        var me = this,
            page = me.getProcesses(),
            sortContainer = page.down('container[name=sortprocessespanel]').getContainer(),
            store = me.getStore('Imt.processes.store.InsightProcessesStore'),
            menu = page.down('#processes-sorting-menu-id'),
            addSortBtn = page.down('#add-sort-btn'),
            sorting,
            menuItem,
            cls;

        sortContainer.removeAll();
        sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        menu.down('[name=processId]').show();
        page.down('#add-sort-btn').enable();

        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {

                if (sortItem.direction) {
                    menuItem = me.getProcesses().down('#processes-sorting-menu-id [name=' + sortItem.property + ']');
                    cls = sortItem.direction === Uni.component.sort.model.Sort.ASC
                        ? 'x-btn-sort-item-asc'
                        : 'x-btn-sort-item-desc';

                    sortContainer.add({
                        xtype: 'sort-item-btn',
                        itemId: 'history-sort-by-' + sortItem.property + '-button',
                        text: menuItem.text,
                        sortType: sortItem.property,
                        sortDirection: sortItem.direction,
                        iconCls: cls
                    });
                    menuItem.hide();

                    if (sortContainer.items.getCount() == menu.totalNumberOfItems){
                        addSortBtn.disable();
                    }
                }
            });
        }
    },

	previewProcess: function (grid, record) {
        var me = this;
        var processes = this.getProcessesGrid().getSelectionModel().getSelection();
        var openTasksValue = "";

        router = me.getController('Uni.controller.history.Router');

        if (processes.length == 1) {
            var process = processes[0];
            /* Load process information on preview form */
            var previewForm = this.getProcessPreviewForm();
            previewForm.loadRecord(process);
            this.getProcessPreview().setTitle(Ext.String.htmlEncode(process.get('name')));

            var type = process.get('type');

            if (type == "Device")
            {
                previewForm.down("#deviceName").setValue(process.get('objectName'));
                previewForm.down("#deviceName").setVisible(true);
                previewForm.down("#usagePointName").setVisible(false);
                previewForm.down("#alarmName").setVisible(false);
                previewForm.down("#issueName").setVisible(false);
                previewForm.down("#deviceForAlarm").setVisible(false);
                previewForm.down("#deviceForIssue").setVisible(false);
            } else if (type == "UsagePoint")
            {
                previewForm.down("#usagePointName").setValue(process.get('objectName'));
                previewForm.down("#usagePointName").setVisible(true);
                previewForm.down("#deviceName").setVisible(false);
                previewForm.down("#alarmName").setVisible(false);
                previewForm.down("#issueName").setVisible(false);
                previewForm.down("#deviceForAlarm").setVisible(false);
                previewForm.down("#deviceForIssue").setVisible(false);
            } else if (type == "Alarm")
            {
                previewForm.down("#alarmName").setValue(process);
                previewForm.down("#alarmName").setVisible(true);
                previewForm.down("#deviceName").setVisible(false);
                previewForm.down("#usagePointName").setVisible(false);
                previewForm.down("#issueName").setVisible(false);
                previewForm.down("#deviceForIssue").setVisible(false);

                previewForm.down("#deviceForAlarm").setValue(process.get('corrDeviceName'));
                previewForm.down("#deviceForAlarm").setVisible(true);
            } else if (type == "Issue")
            {
                previewForm.down("#issueName").setRawValue(process);
                previewForm.down("#issueName").setVisible(true);
                previewForm.down("#deviceForIssue").setValue(process.get('corrDeviceName'));
                previewForm.down("#deviceForIssue").setVisible(true);

                previewForm.down("#deviceName").setVisible(false);
                previewForm.down("#usagePointName").setVisible(false);
                previewForm.down("#alarmName").setVisible(false);
                previewForm.down("#deviceForAlarm").setVisible(false);
            } else {
                previewForm.down("#deviceName").setVisible(false);
                previewForm.down("#usagePointName").setVisible(false);
                previewForm.down("#alarmName").setVisible(false);
                previewForm.down("#issueName").setVisible(false);
                previewForm.down("#deviceForAlarm").setVisible(false);
                previewForm.down("#deviceForIssue").setVisible(false);
            }

            /* For status preview */
           	this.showNodesDetailsWithSubprocesses(process, this.getProcessStatusPreviewExtendedGrid(), this.getParentProcessPreviewExtendedPanel(), this.getChildProcessPreviewExtendedPanel(), this.getStatusVariablesPreviewExtendedPanel());
            
            /* Prepare user tasks to show */
            process.openTasks().each(function (rec) {
                if (openTasksValue.length > 0) {
                    openTasksValue += '<br>';
                }

                var taskName = rec.get('name').length > 0 ? rec.get('name') : Uni.I18n.translate('imt.process.noTaskName', 'IMT', 'No task name'),
                    status = rec.get('statusDisplay'),
                    assign = rec.get('actualOwner').length > 0 ? rec.get('actualOwner') : Uni.I18n.translate('imt.process.unassigned', 'IMT', 'Unassigned');

                    if (Bpm.privileges.BpmManagement.canView()){
                        openTasksValue += Ext.String.format('<a href =\"{0}\">{1}</a> ({2}, {3})',
                        router.getRoute('workspace/tasks/task').buildUrl({taskId: rec.get('id')}, {showNavigation: false}),
                        Ext.String.htmlEncode(taskName), status, assign);
                    }else{
                        openTasksValue += Ext.String.format('{0}({1}, {2})',Ext.String.htmlEncode(taskName), status, assign);
                    }
            });
            this.getOpenTasksDisplay().setValue((openTasksValue.length > 0) ? openTasksValue : Uni.I18n.translate('imt.process.noOpenTasks', 'IMT', 'None'));

        }
    },

	showNodesDetailsWithSubprocesses: function (processRecord, grid, parentProcessPanel, childProcessPanel, variablesValuesPanel) {
        var me = this;
        var extendedProcessNodesModel = Ext.ModelManager.getModel('Bpm.monitorprocesses.model.ExtendedProcessNodes');
        var parentProcessModel = Ext.ModelManager.getModel('Bpm.monitorprocesses.model.ParentProcess');

		grid.setLoading(true);
		parentProcessPanel.setLoading(true);
		childProcessPanel.setLoading(true);
		variablesValuesPanel.setLoading(true);

        Ext.Ajax.request({
            url: Ext.String.format('../../api/bpm/runtime/process/instance/{0}/nodeswithsubprocessinfo', processRecord.get('processId')),
            method: 'GET',
            success: function (option) {
                var response = Ext.JSON.decode(option.responseText),
                    reader = Bpm.monitorprocesses.model.ExtendedProcessNodes.getProxy().getReader(),
                    resultSet = reader.readRecords(response),
                    record = resultSet.records[0];
                if (grid){
                    grid.reconfigure(record.list());
                    grid.getSelectionModel().select(0);
                    grid.setLoading(false);
                    childProcessPanel.setLoading(false);
					variablesValuesPanel.setLoading(false);
                }

            }
        })
        
        Ext.Ajax.request({
        	url: Ext.String.format('../../api/bpm/runtime/process/instance/{0}/parent', processRecord.get('processId')),
        	method: 'GET',
        	success: function (option) {
        	
        		var panelItems = new Ext.util.MixedCollection();
				if(option.responseText) {
				
					var response = Ext.JSON.decode(option.responseText),
                    	reader = Bpm.monitorprocesses.model.ParentProcess.getProxy().getReader(),
						resultSet = reader.readRecords(response),
						record = resultSet.records[0];
				
					panelItems.add(Ext.create("Ext.form.field.Display", {
							fieldLabel: Uni.I18n.translate('imt.process.parentProcessInstanceId', 'IMT', 'Parent process instance id'),
							style: '{word-break: break-word; word-wrap: break-word;}',
							flex: 1,
							labelWidth: 200,
							htmlEncode: false,
							value: '<a>' + record.get('processInstanceId') + '</a>',
							listeners: {
								afterrender: function(view) {
									view.getEl().on('click', function() {
										var router = me.getController('Uni.controller.history.Router');
										var route = router.getRoute('workspace/insightprocesses');
										route.forwardInNewTab(null, {processInstanceId: [record.get('processInstanceId')], searchInAllProcesses: true});
									});
								}
							}
						}
					));
					panelItems.add(Ext.create("Ext.form.field.Display", {
							fieldLabel: Uni.I18n.translate('imt.process.parentProcessName', 'IMT', 'Parent process name'),
							style: '{word-break: break-word; word-wrap: break-word;}',
							flex: 1,
							labelWidth: 200,
							value: record.get('processName')
						}
					));
					parentProcessPanel.removeAll();
					parentProcessPanel.items = panelItems;
					parentProcessPanel.doLayout();
				} else {
					panelItems.add(Ext.create("Ext.Component", {
                    height: 40,
                    autoEl: {
                        html: Uni.I18n.translate('imt.process.noParentProcess', 'IMT', 'The specified process has no parent process'),
                        tag: 'span',
                        style: {
                            top: '2em !important',
                            fontStyle: 'italic',
                            color: '#999'
								}
							}
						}
					));
					parentProcessPanel.removeAll();
					parentProcessPanel.items = panelItems;
					parentProcessPanel.doLayout();
				}
				parentProcessPanel.setLoading(false);
            }
        })
    },
    
    showVariablesPreviewExtendedForStatus: function (selectionModel, record) {
        var me = this;
        return me.showVariablesPreviewExtended(me.getStatusVariablesPreviewExtendedPanel(), me.getChildProcessPreviewExtendedPanel(), record);
    },

   showVariablesPreviewExtended: function (panel, subprocessPanel, record) {
        var me = this;

        panel.setTitle(Ext.String.format(Uni.I18n.translate('imt.process.node.variablesTitle', 'IMT', '{0} ({1}) variables'),
            record.get('nodeInfo.name'), record.get('nodeInfo.type')));
            
        subprocessPanel.setTitle(Ext.String.format(Uni.I18n.translate('imt.process.node.subprocessesTitle', 'IMT', '{0} ({1}) subprocesses'),
            record.get('nodeInfo.name'), record.get('nodeInfo.type')));

        var panelItems = new Ext.util.MixedCollection(), subprocessPanelItems = new Ext.util.MixedCollection();


        if (record.get('nodeInfo.processInstanceVariables') && record.get('nodeInfo.processInstanceVariables').length > 0) {
            Ext.Array.each(record.get('nodeInfo.processInstanceVariables'), function (variable) {
                panelItems.add(Ext.create("Ext.form.field.Display", {
                        fieldLabel: variable.variableName,
                        style: '{word-break: break-word; word-wrap: break-word;}',
                        flex: 1,
                        labelWidth: 150,
                        value: variable.value
                    }
                ))
            });
        }
        else {
            panelItems.add(Ext.create("Ext.Component", {
                    height: 40,
                    autoEl: {
                        html: Uni.I18n.translate('imt.process.node.noVariables', 'IMT', 'No variable change during the node execution'),
                        tag: 'span',
                        style: {
                            top: '2em !important',
                            fontStyle: 'italic',
                            color: '#999'
                        }
                    }
                }
            ))
        }
        
        if(record.get('childSubprocessLog.childProcessInstanceId')) {
        	subprocessPanelItems.add(Ext.create("Ext.form.field.Display", {
                        fieldLabel: Uni.I18n.translate('imt.process.node.childProcessInstanceId', 'IMT', 'Child process instance id'),
                        style: '{word-break: break-word; word-wrap: break-word;}',
                        flex: 1,
                        labelWidth: 200,
                        htmlEncode: false,
                        value: '<a>' + record.get('childSubprocessLog.childProcessInstanceId') + '</a>',
                        listeners: {
                        	afterrender: function(view) {
                        		view.getEl().on('click', function() {
                        			var router = me.getController('Uni.controller.history.Router');
                        			var route = router.getRoute('workspace/insightprocesses');
                        			route.forwardInNewTab(null, {processInstanceId: [record.get('childSubprocessLog.childProcessInstanceId')], searchInAllProcesses: true});
                        		});
                        	}
                        }
                    }
                ));
            subprocessPanelItems.add(Ext.create("Ext.form.field.Display", {
                        fieldLabel: Uni.I18n.translate('imt.process.node.childProcessName', 'IMT', 'Child process name'),
                        style: '{word-break: break-word; word-wrap: break-word;}',
                        flex: 1,
                        labelWidth: 200,
                        value: record.get('childSubprocessLog.processName')
                    }
                ));        
        } else {
			subprocessPanelItems.add(Ext.create("Ext.Component", {
                    height: 40,
                    autoEl: {
                        html: Uni.I18n.translate('imt.process.node.noChildProcessInNode', 'IMT', 'No child process started in this node'),
                        tag: 'span',
                        style: {
                            top: '2em !important',
                            fontStyle: 'italic',
                            color: '#999'
                        }
                    }
                }
            ));
		}

        panel.removeAll();
        subprocessPanel.removeAll();
        panel.items = panelItems;
        subprocessPanel.items = subprocessPanelItems;
        panel.doLayout();
        subprocessPanel.doLayout();
    },
    
    /* Functions for sorting */
   chooseSort: function (menu, item) {
           var me = this,
               name = item.name,
               store = me.getStore('Imt.processes.store.InsightProcessesStore'),
               sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']),
               sortingItem;

           if (Ext.isArray(sorting)) {
               sortingItem = Ext.Array.findBy(sorting, function (item) {
                   return item.property === name
               });
               if (sortingItem) {
                   return;
               } else {
                   sorting.push({
                       property: name,
                       direction: Uni.component.sort.model.Sort.DESC
                   });
               }
           } else {
               sorting = [
                   {
                       property: name,
                       direction: Uni.component.sort.model.Sort.DESC
                   }
               ];
           }

           store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
           me.updateSortingToolbarAndResults();
   },

    updateSortingToolbarAndResults: function() {
        var me = this,
        store = me.getStore('Imt.processes.store.InsightProcessesStore');
        me.updateSortingToolbar();
        store.load();
    },

    updateSortingToolbar: function () {
        var me = this,
            page = me.getProcesses(),
            sortContainer = page.down('container[name=sortprocessespanel]').getContainer(),
            store = me.getStore('Imt.processes.store.InsightProcessesStore'),
            menu = page.down('#processes-sorting-menu-id'),
            addSortBtn = page.down('#add-sort-btn'),
            sorting,
            menuItem,
            cls;

        sortContainer.removeAll();
        sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']);

        menu.down('[name=processId]').show();
        page.down('#add-sort-btn').enable();

        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {

                if (sortItem.direction) {
                    menuItem = me.getProcesses().down('#processes-sorting-menu-id [name=' + sortItem.property + ']');
                    cls = sortItem.direction === Uni.component.sort.model.Sort.ASC
                        ? 'x-btn-sort-item-asc'
                        : 'x-btn-sort-item-desc';

                    sortContainer.add({
                        xtype: 'sort-item-btn',
                        itemId: 'history-sort-by-' + sortItem.property + '-button',
                        text: menuItem.text,
                        sortType: sortItem.property,
                        sortDirection: sortItem.direction,
                        iconCls: cls
                    });
                    menuItem.hide();

                    if (sortContainer.items.getCount() == menu.totalNumberOfItems){
                        addSortBtn.disable();
                    }
                }
            });
        }
    },

    setDefaultSort: function () {
        var me = this,
            store = me.getStore('Imt.processes.store.InsightProcessesStore'),
            sorting = store.getProxy().extraParams['sort'];

        if (sorting === undefined) { // set default filters
            sorting = [];
            sorting.push({
                property: 'processId',
                direction: Uni.component.sort.model.Sort.DESC
            });
            store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        }
    },


    switchSortingOrder: function (btn) {
        var me = this,
            store = me.getStore('Imt.processes.store.InsightProcessesStore'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']),
            sortingItem;

        if (Ext.isArray(sorting)) {
            sortingItem = Ext.Array.findBy(sorting, function (item) {
                return item.property === btn.sortType
            });
            if (sortingItem) {
                if (sortingItem.direction === Uni.component.sort.model.Sort.ASC) {
                    sortingItem.direction = Uni.component.sort.model.Sort.DESC;
                } else {
                    sortingItem.direction = Uni.component.sort.model.Sort.ASC;
                }
            }
        }
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    },

    clearAllSorting: function (btn) {
        var me = this,
            store = me.getStore('Imt.processes.store.InsightProcessesStore'),
            page = me.getProcesses(),
            menu = page.down('#processes-sorting-menu-id'),
            sorting;

        sorting = [];
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        menu.activeSortingImtes = 0;
        me.updateSortingToolbarAndResults();
    },

    onSortCloseClicked: function (btn) {
        var me = this,
            store = me.getStore('Imt.processes.store.InsightProcessesStore'),
            sorting = Ext.JSON.decode(store.getProxy().extraParams['sort']),
            page = me.getProcesses(),
            menu = page.down('#processes-sorting-menu-id');

        if (Ext.isArray(sorting)) {
            Ext.Array.remove(sorting, Ext.Array.findBy(sorting, function (item) {
                return item.property === btn.sortType
            }));
        }

        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sorting));
        me.updateSortingToolbarAndResults();
    }
});
