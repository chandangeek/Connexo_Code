Ext.define('Bpm.monitorissueprocesses.controller.MonitorIssueProcesses', {
    extend: 'Ext.app.Controller',
    requires: [],
    models: [
        'Bpm.monitorissueprocesses.model.ProcessNodes'
    ],
    stores: [
        'Bpm.monitorissueprocesses.store.IssueProcesses'
    ],
    views: [
        'Bpm.monitorissueprocesses.view.IssueProcessesMainView'
    ],
    refs: [
        {ref: 'mainPage', selector: 'bpm-issue-processes-main-view'},
        {ref: 'issueProcessesGrid', selector: '#issue-processes-grid'},
        {ref: 'processNodesGrid', selector: '#issue-process-preview #process-nodes-grid'},
        {ref: 'variablesPreviewPanel', selector: '#issue-process-preview #node-variables-preview-panel'},
    ],

    init: function () {
        var me = this;
        me.control({
            'bpm-issue-processes-main-view': {
                initComponents: this.initComponents,
                initStores: this.initStores
            },
            'bpm-issue-processes #issue-processes-grid': {
                select: this.showProcessPreview
            },
            '#issue-process-preview #process-nodes-grid': {
                select: this.showVariablesPreview
            },

        });
    },

    initComponents: function (component) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            record;

        record = me.getIssueProcessesGrid().getStore().findRecord('processId', router.getRoute().params.process);
        me.getIssueProcessesGrid().getSelectionModel().select(record);

    },

    initStores: function (issueId) {
        var me = this;
        me.getStore('Bpm.monitorissueprocesses.store.IssueProcesses').getProxy().setUrl(issueId);
    },
    showProcesses:function (selectionModel, record) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            processStore = me.getStore('Bpm.monitorissueprocesses.store.IssueProcesses'),
            widget;

        viewport.setLoading();

        widget = Ext.widget('bpm-issue-processes-main-view', {
            selectedProcess: router.getRoute().params.process,
            properties: {
                route: router.getRoute()
            }
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        viewport.setLoading(false);

    },
    showProcessPreview: function (selectionModel, record) {
        var me = this,
            mainPage = me.getMainPage(),
            previewRunningDetails = mainPage.down('bpm-issue-process-preview'),
            previewRunningDetailsForm = mainPage.down('#frm-preview-running-process'),
            router = me.getController('Uni.controller.history.Router'),
            openTasksValue = "";


        Ext.suspendLayouts();

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

        me.showNodesDetails(record, me.getProcessNodesGrid());
        Ext.resumeLayouts();

    },
    showNodesDetails: function (processRecord, grid) {
        var me = this,
            mainPage = me.getMainPage(),
            previewStatusProcess = mainPage.down('#status-process-preview');

        var processNodesModel = Ext.ModelManager.getModel('Bpm.monitorissueprocesses.model.ProcessNodes');

        Ext.Ajax.request({
            url: Ext.String.format('../../api/bpm/runtime/process/instance/{0}/nodes', processRecord.get('processId')),
            method: 'GET',
            success: function (option) {
                var response = Ext.JSON.decode(option.responseText),
                    reader = Bpm.monitorissueprocesses.model.ProcessNodes.getProxy().getReader(),
                    resultSet = reader.readRecords(response),
                    record = resultSet.records[0];

                grid.reconfigure(record.processInstanceNodes());
                grid.getSelectionModel().select(0);
            }
        })
    },

    showVariablesPreview: function (panel, record) {
        var me = this,
        previewPanel = me.getVariablesPreviewPanel();

        previewPanel.setTitle(Ext.String.format(Uni.I18n.translate('bpm.process.node.variablesTitle', 'BPM', '{0} ({1}) variables'),
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

        previewPanel.removeAll();
        previewPanel.items = panelItems;
        previewPanel.doLayout();
    },

});