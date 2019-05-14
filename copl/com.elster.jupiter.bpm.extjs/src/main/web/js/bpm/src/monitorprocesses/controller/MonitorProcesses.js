/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.controller.MonitorProcesses', {
    extend: 'Ext.app.Controller',
    requires: [],
    models: [
        'Bpm.monitorprocesses.model.HistoryProcessesFilter',
        'Bpm.monitorprocesses.model.ProcessNodes',
        'Bpm.monitorprocesses.model.ExtendedProcessNodes',
        'Bpm.monitorprocesses.model.ParentProcess'
    ],
    stores: [
        'Bpm.monitorprocesses.store.RunningProcesses',
        'Bpm.monitorprocesses.store.HistoryProcesses',
        'Bpm.monitorprocesses.store.HistoryProcessesFilterProcesses',
        'Bpm.monitorprocesses.store.HistoryProcessesFilterStatuses',
        'Bpm.monitorprocesses.store.HistoryProcessesFilterUsers'

    ],
    views: [
        //'Bpm.monitorprocesses.view.SideMenu',
        'Bpm.monitorprocesses.view.MonitorProcessesMainView'
    ],
    refs: [
        {ref: 'mainPage', selector: 'bpm-monitor-processes-main-view'},
        {ref: 'processesTab', selector: '#tab-processes'},
        {ref: 'historyProcessesGrid', selector: '#history-processes-grid'},
        {ref: 'runningProcessesGrid', selector: '#running-processes-grid'},
        {ref: 'openTasksDisplay', selector: '#bpm-preview-running-process-open-tasks'},
        {ref: 'taskNodesDisplay', selector: '#bpm-preview-task-nodes'},
        {ref: 'runningProcessNodesGrid', selector: '#running-process-status-preview #process-nodes-grid'},
        {ref: 'historyProcessNodesGrid', selector: '#history-process-status-preview #process-nodes-grid'},
        {ref: 'runningVariablesPreviewPanel', selector: '#running-process-status-preview #node-variables-preview-panel'},
        {ref: 'historyVariablesPreviewPanel', selector: '#history-process-status-preview #node-variables-preview-panel'},
        {ref: 'runningProcessStatusPreviewExtendedTab', selector: '#running-process-status-preview-extended'},
        {ref: 'historyProcessStatusPreviewExtendedTab', selector: '#history-process-status-preview-extended'},
        {ref: 'runningProcessNodesExtendedGrid', selector: '#running-process-status-preview-extended #process-nodes-grid-extended'},
        {ref: 'historyProcessNodesExtendedGrid', selector: '#history-process-status-preview-extended #process-nodes-grid-extended'},
        {ref: 'runningStatusVariablesPreviewExtendedPanel', selector: '#running-process-status-preview-extended #node-variables-preview-panel'},
        {ref: 'historyStatusVariablesPreviewExtendedPanel', selector: '#history-process-status-preview-extended #node-variables-preview-panel'},
        {ref: 'runningChildProcessPreviewExtendedPanel', selector: '#running-process-status-preview-extended #child-process-preview-panel'},
        {ref: 'historyChildProcessPreviewExtendedPanel', selector: '#history-process-status-preview-extended #child-process-preview-panel'},
		{ref: 'runningParentProcessPreviewExtendedPanel', selector: '#running-process-status-preview-extended #parent-process-preview-panel'},
		{ref: 'historyParentProcessPreviewExtendedPanel', selector: '#history-process-status-preview-extended #parent-process-preview-panel'}
    ],

    init: function () {
        var me = this;
        me.control({
            'bpm-monitor-processes-main-view': {
                initComponents: this.initComponents,
                initStores: this.initStores
            },
            'bpm-running-processes #running-processes-grid': {
                select: this.showRunningPreview
            },
            'bpm-history-processes #history-processes-grid': {
                select: this.showHistoryPreview
            },
            '#tab-processes': {
                tabChange: this.changeTab
            },
            '#running-process-status-preview #process-nodes-grid': {
                select: this.showVariablesPreviewForRunning
            },
            '#history-process-status-preview #process-nodes-grid': {
                select: this.showVariablesPreviewForHistory
            },
            '#running-process-status-preview-extended #process-nodes-grid-extended': {
                select: this.showExtendedVariablesPreviewForRunning
            },
            '#history-process-status-preview-extended #process-nodes-grid-extended': {
                select: this.showExtendedVariablesPreviewForHistory
            }
        });
    },

    initComponents: function (component) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        me.getProcessesTab().setActiveTab(queryString.activeTab == 'history' ? 1 : 0);

        me.getHistoryProcessesGrid().getStore().on('datachanged', function () {
            me.getHistoryProcessesGrid().getSelectionModel().select(0);
            return true;
        }, this);
    },

    initStores: function (properties) {
        var me = this;

        me.getStore('Bpm.monitorprocesses.store.RunningProcesses').getProxy().setUrl(properties.variableId, properties.value);
        me.getStore('Bpm.monitorprocesses.store.HistoryProcesses').getProxy().setUrl(properties.variableId, properties.value);
        me.getStore('Bpm.monitorprocesses.store.HistoryProcessesFilterProcesses').getProxy().extraParams = {type: properties.name};
    },

    showRunningPreview: function (selectionModel, record) {
        var me = this,
            mainPage = me.getMainPage(),
            previewRunningDetails = mainPage.down('bpm-running-process-preview'),
            previewHistoryDetails = mainPage.down('bpm-history-process-preview'),
            previewRunningDetailsForm = mainPage.down('#frm-preview-running-process'),
            router = me.getController('Uni.controller.history.Router'),
            openTasksValue = "";

        Ext.suspendLayouts();
        previewRunningDetails.setTitle(record.get('name'));
        previewRunningDetailsForm.loadRecord(record);

        record.openTasks().each(function (rec) {
            if (openTasksValue.length > 0) {
                openTasksValue += '<br>';
            }

            var taskName = rec.get('name').length > 0 ? rec.get('name') : Uni.I18n.translate('bpm.process.noTaskName', 'BPM', 'No task name'),
                status = rec.get('statusDisplay'),
                assign = rec.get('actualOwner').length > 0 ? rec.get('actualOwner') : Uni.I18n.translate('bpm.process.unassigned', 'BPM', 'Unassigned');

            if (mainPage.properties.route) {
                openTasksValue += Ext.String.format('<a href =\"{0}\">{1}</a> ({2}, {3})',
                    router.getRoute(mainPage.properties.route).buildUrl({taskId: rec.get('id')}, {showNavigation: false}),
                    Ext.String.htmlEncode(taskName), status, assign);
            }
            else {
                openTasksValue += Ext.String.format('{0} ({1}, {2})', Ext.String.htmlEncode(taskName), status, assign);
            }
        });

        me.getOpenTasksDisplay().setValue((openTasksValue.length > 0) ? openTasksValue : Uni.I18n.translate('bpm.process.noOpenTasks', 'BPM', 'None'));
        if (me.getRunningProcessStatusPreviewExtendedTab()) {
        	me.showNodesDetailsWithSubprocesses(record, me.getRunningProcessNodesExtendedGrid(), me.getRunningParentProcessPreviewExtendedPanel());
        } else {
        	me.showNodesDetails(record, me.getRunningProcessNodesGrid());
        }
        Ext.resumeLayouts();
    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            mainPage = me.getMainPage(),
            previewRunningDetails = mainPage.down('bpm-running-process-preview'),
            previewHistoryDetails = mainPage.down('bpm-history-process-preview'),
            previewHistoryDetailsForm = mainPage.down('#frm-preview-history-process');

        Ext.suspendLayouts();
        previewHistoryDetails.setTitle(record.get('name'));
        previewHistoryDetailsForm.loadRecord(record);
        if (me.getHistoryProcessStatusPreviewExtendedTab()) {
        	me.showNodesDetailsWithSubprocesses(record, me.getHistoryProcessNodesExtendedGrid(), me.getHistoryParentProcessPreviewExtendedPanel());
        } else {
        	me.showNodesDetails(record, me.getHistoryProcessNodesGrid());
        }

        Ext.resumeLayouts();
    },

    showNodesDetails: function (processRecord, grid) {
        var me = this,
            mainPage = me.getMainPage(),
            previewStatusProcess = mainPage.down('#status-process-preview');

        // previewStatusProcess.setLoading(true);
        var processNodesModel = Ext.ModelManager.getModel('Bpm.monitorprocesses.model.ProcessNodes');

        Ext.Ajax.request({
            url: Ext.String.format('../../api/bpm/runtime/process/instance/{0}/nodes', processRecord.get('processId')),
            method: 'GET',
            success: function (option) {
                var response = Ext.JSON.decode(option.responseText),
                    reader = Bpm.monitorprocesses.model.ProcessNodes.getProxy().getReader(),
                    resultSet = reader.readRecords(response),
                    record = resultSet.records[0];

                grid.reconfigure(record.processInstanceNodes());
                grid.getSelectionModel().select(0);
            }
        })
    },
    
    showNodesDetailsWithSubprocesses: function (processRecord, grid, parentProcessPanel) {
        var me = this;
        var extendedProcessNodesModel = Ext.ModelManager.getModel('Bpm.monitorprocesses.model.ExtendedProcessNodes');
        var parentProcessModel = Ext.ModelManager.getModel('Bpm.monitorprocesses.model.ParentProcess');

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
							fieldLabel: Uni.I18n.translate('bpm.process.parentProcessInstanceId', 'BPM', 'Parent process instance id'),
							style: '{word-break: break-word; word-wrap: break-word;}',
							flex: 1,
							labelWidth: 200,
							htmlEncode: false,
							value: '<a>' + record.get('processInstanceId') + '</a>',
							listeners: {
								afterrender: function(view) {
									view.getEl().on('click', function() {
										var router = me.getController('Uni.controller.history.Router');
										var route = router.getRoute('workspace/multisenseprocesses');
										route.forwardInNewTab(null, {processInstanceId: [record.get('processInstanceId')], searchInAllProcesses: true});
									});
								}
							}
						}
					));
					panelItems.add(Ext.create("Ext.form.field.Display", {
							fieldLabel: Uni.I18n.translate('bpm.process.parentProcessName', 'BPM', 'Parent process name'),
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
                        html: Uni.I18n.translate('bpm.process.noParentProcess', 'BPM', 'The specified process has no parent process'),
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
            }
        })
    },

    showVariablesPreviewForRunning: function (selectionModel, record) {
        var me = this;
        return me.showVariablesPreview(me.getRunningVariablesPreviewPanel(), record);
    },

    showVariablesPreviewForHistory: function (selectionModel, record) {
        var me = this;
        return me.showVariablesPreview(me.getHistoryVariablesPreviewPanel(), record);
    },

    showVariablesPreview: function (panel, record) {
        var me = this;

        panel.setTitle(Ext.String.format(Uni.I18n.translate('bpm.process.node.variablesTitle', 'BPM', '{0} ({1}) variables'),
            record.get('name'), record.get('type')));

        var panelItems = new Ext.util.MixedCollection();


        if (record.get('processInstanceVariables') && record.get('processInstanceVariables').length > 0) {
            Ext.Array.each(record.get('processInstanceVariables'), function (variable) {
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
                        html: Uni.I18n.translate('bpm.process.node.noVariables', 'BPM', 'No variable change during the node execution'),
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

        panel.removeAll();
        panel.items = panelItems;
        panel.doLayout();
    },
    
    showExtendedVariablesPreviewForRunning: function (selectionModel, record) {
        var me = this;
        return me.showVariablesPreviewExtended(me.getRunningStatusVariablesPreviewExtendedPanel(), me.getRunningChildProcessPreviewExtendedPanel(), record);
    },
    
    showExtendedVariablesPreviewForHistory: function (selectionModel, record) {
        var me = this;
        return me.showVariablesPreviewExtended(me.getHistoryStatusVariablesPreviewExtendedPanel(), me.getHistoryChildProcessPreviewExtendedPanel(), record);
    },
    
    showVariablesPreviewExtended: function (panel, subprocessPanel, record) {
        var me = this;

        panel.setTitle(Ext.String.format(Uni.I18n.translate('bpm.process.node.variablesTitle', 'BPM', '{0} ({1}) variables'),
            record.get('nodeInfo.name'), record.get('nodeInfo.type')));
            
        subprocessPanel.setTitle(Ext.String.format(Uni.I18n.translate('bpm.process.node.subprocessesTitle', 'BPM', '{0} ({1}) subprocesses'),
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
                        html: Uni.I18n.translate('bpm.process.node.noVariables', 'BPM', 'No variable change during the node execution'),
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
                        fieldLabel: Uni.I18n.translate('bpm.process.node.childProcessInstanceId', 'BPM', 'Child process instance id'),
                        style: '{word-break: break-word; word-wrap: break-word;}',
                        flex: 1,
                        labelWidth: 200,
                        htmlEncode: false,
                        value: '<a>' + record.get('childSubprocessLog.childProcessInstanceId') + '</a>',
                        listeners: {
                        	afterrender: function(view) {
                        		view.getEl().on('click', function() {
                        			var router = me.getController('Uni.controller.history.Router');
                        			var route = router.getRoute('workspace/multisenseprocesses');
                        			route.forwardInNewTab(null, {processInstanceId: [record.get('childSubprocessLog.childProcessInstanceId')], searchInAllProcesses: true});
                        		});
                        	}
                        }
                    }
                ));
            subprocessPanelItems.add(Ext.create("Ext.form.field.Display", {
                        fieldLabel: Uni.I18n.translate('bpm.process.node.childProcessName', 'BPM', 'Child process name'),
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
                        html: Uni.I18n.translate('bpm.process.node.noChildProcessInNode', 'BPM', 'No child process started in this node'),
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
    
    applyNewState: function (queryString) {
        var me = this,
            href = Uni.util.QueryString.buildHrefWithQueryString(queryString, false);

        if (window.location.href !== href) {
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            window.location.href = href;
            Ext.util.History.currentToken = window.location.hash.substr(1);
        }
    },

    changeTab: function (tabPanel, tab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        queryString.activeTab = (tab.itemId === 'running-processes-tab') ? 'running' : 'history';

        var selectionModel = me.getHistoryProcessesGrid().getSelectionModel();
        if ((tab.itemId === 'history-processes-tab')
            && me.getHistoryProcessesGrid().getStore().getCount() > 0) {
            if (selectionModel.getCount() == 0) {
                selectionModel.select(0);
            }
            else {
                me.showHistoryPreview(selectionModel, selectionModel.getSelection()[0]);
            }
        }

        selectionModel = me.getRunningProcessesGrid().getSelectionModel();
        if ((tab.itemId === 'running-processes-tab')
            && me.getRunningProcessesGrid().getStore().getCount() > 0) {
            if (selectionModel.getCount() == 0) {
                selectionModel.select(0);
            }
            else {
                me.showRunningPreview(selectionModel, selectionModel.getSelection()[0]);
            }
        }
        me.applyNewState(queryString);
    }
});