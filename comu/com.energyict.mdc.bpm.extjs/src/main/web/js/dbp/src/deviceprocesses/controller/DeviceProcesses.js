Ext.define('Dbp.deviceprocesses.controller.DeviceProcesses', {
    extend: 'Ext.app.Controller',
    requires: [],
    models: [
        'Dbp.deviceprocesses.model.HistoryProcessesFilter',
        'Dbp.deviceprocesses.model.ProcessNodes'
    ],
    stores: [
        'Dbp.deviceprocesses.store.RunningProcesses',
        'Dbp.deviceprocesses.store.HistoryProcesses',
        'Dbp.deviceprocesses.store.HistoryProcessesFilterProcesses',
        'Dbp.deviceprocesses.store.HistoryProcessesFilterStatuses',
        'Dbp.deviceprocesses.store.HistoryProcessesFilterUsers'

    ],
    views: [
        'Dbp.deviceprocesses.view.SideMenu',
        'Dbp.deviceprocesses.view.DeviceProcessesMainView'
    ],
    refs: [
        {ref: 'mainPage', selector: 'dbp-device-processes-main-view'},
        {ref: 'processesTab', selector: '#tab-processes'},
        {ref: 'historyProcessesGrid', selector: '#history-processes-grid'},
        {ref: 'runningProcessesGrid', selector: '#running-processes-grid'},
        {ref: 'openTasksDisplay', selector: '#dbp-preview-running-process-open-tasks'},
        {ref: 'taskNodesDisplay', selector: '#dbp-preview-task-nodes'},
        {ref: 'taskNodesForm', selector: '#dbp-preview-task-nodes-form'}
    ],
    mRID: null,

    init: function () {
        var me = this;
        me.control({
            'dbp-running-processes #running-processes-grid': {
                select: this.showRunningPreview
            },
            'dbp-history-processes #history-processes-grid': {
                select: this.showHistoryPreview
            },
            '#tab-processes': {
                tabChange: this.changeTab
            }
        });
    },

    showDeviceProcesses: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.mRID = mRID;
        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var widget = me.getMainPage();

                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);

                me.getStore('Dbp.deviceprocesses.store.RunningProcesses').getProxy().setUrl('mrid', me.mRID);
                me.getStore('Dbp.deviceprocesses.store.HistoryProcesses').getProxy().setUrl('mrid', me.mRID);

                widget = Ext.widget('dbp-device-processes-main-view', {device: device});
                me.getApplication().fireEvent('changecontentevent', widget);

                var queryString = Uni.util.QueryString.getQueryStringValues(false);
                me.getProcessesTab().setActiveTab(queryString.activeTab == 'history' ? 1 : 0);

                me.getHistoryProcessesGrid().getStore().on('datachanged', function () {
                    me.getHistoryProcessesGrid().getSelectionModel().select(0);
                    return true;
                }, this);
            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });

    },

    showRunningPreview: function (selectionModel, record) {
        var me = this,
            mainPage = me.getMainPage(),
            previewRunningDetails = mainPage.down('dbp-running-process-preview'),
            previewHistoryDetails = mainPage.down('dbp-history-process-preview'),
            previewRunningDetailsForm = mainPage.down('#frm-preview-running-process'),
            router = me.getController('Uni.controller.history.Router'),
            openTasksValue = "";

        Ext.suspendLayouts();
        previewRunningDetails.setVisible(true);
        previewHistoryDetails.setVisible(false);
        previewRunningDetails.setTitle(record.get('name'));
        previewRunningDetailsForm.loadRecord(record);

        record.openTasks().each(function (rec) {
            if (openTasksValue.length > 0) {
                openTasksValue += '<br>';
            }

            var taskName = rec.get('name').length > 0 ? rec.get('name') : Uni.I18n.translate('dbp.process.noTaskName', 'DBP', 'No task name'),
                status = rec.get('statusDisplay'),
                assign = rec.get('actualOwner').length > 0 ? rec.get('actualOwner') : Uni.I18n.translate('dbp.process.unassigned', 'DBP', 'Unassigned');

            if (Dbp.privileges.DeviceProcesses.canAssignOrExecute()) {
                openTasksValue += Ext.String.format('<a href =\"{0}\">{1}</a> ({2}, {3})',
                    router.getRoute('workspace/tasks/performTask').buildUrl({taskId: rec.get('id')}, {showNavigation: false}),
                    Ext.String.htmlEncode(taskName), status, assign);
            }
            else {
                openTasksValue += Ext.String.format('{0} ({1}, {2})', Ext.String.htmlEncode(taskName), status, assign);
            }
        });

        me.getOpenTasksDisplay().setValue((openTasksValue.length > 0) ? openTasksValue : Uni.I18n.translate('dbp.process.noOpenTasks', 'DBP', 'None'));
        me.showNodesDetails(record);
        Ext.resumeLayouts();
    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            mainPage = me.getMainPage(),
            previewRunningDetails = mainPage.down('dbp-running-process-preview'),
            previewHistoryDetails = mainPage.down('dbp-history-process-preview'),
            previewHistoryDetailsForm = mainPage.down('#frm-preview-history-process');

        Ext.suspendLayouts();
        previewRunningDetails.setVisible(false);
        previewHistoryDetails.setVisible(true);
        previewHistoryDetails.setTitle(record.get('name'));
        previewHistoryDetailsForm.loadRecord(record);
        me.showNodesDetails(record);
        Ext.resumeLayouts();
    },

    showNodesDetails: function (deviceProcessRecord) {
        var me = this,
            mainPage = me.getMainPage(),
            previewStatusProcess = mainPage.down('#status-process-preview');

        // previewStatusProcess.setLoading(true);
        var processNodesModel = Ext.ModelManager.getModel('Dbp.deviceprocesses.model.ProcessNodes');

        Ext.Ajax.request({
            url: Ext.String.format('../../api/bpm/runtime/process/instance/{0}/nodes', deviceProcessRecord.get('processId')),
            method: 'GET',
            success: function (operation) {
                formItems = new Ext.util.MixedCollection();
                var processNodes = new Dbp.deviceprocesses.model.ProcessNodes(Ext.decode(operation.responseText));
                Ext.Array.each(processNodes.get('processInstanceNodes'), function (node) {
                    var logDate = parseInt(node.logDate),
                        logDateString = logDate ? Uni.DateTime.formatDateTimeShort(new Date(logDate)) : '-',
                        nodeName = node.nodeName,
                        nodeType = node.nodeType,
                        status = node.status,
                        iconCls = (status === 'COMPLETED') ? 'icon-checkmark' :
                            (status === 'ABORTED') ? 'icon-close' : (status === 'ACTIVE') ? 'icon-pause' : '';

                    formItems.add(Ext.create("Ext.form.Panel", {
                            layout: {
                                type: 'hbox',
                                align: 'middle'
                            },
                            items: [
                                {
                                    fieldLabel: '&nbsp;',
                                    flex: 0.1,
                                    xtype: 'displayfield',
                                    labelCls: 'communication-tasks-status ' + iconCls,
                                    labelStyle: 'position: absolute; top: 25%;'
                                },
                                {
                                    fieldLabel: '&nbsp;',
                                    flex: 0.8,
                                    labelWidth: 0,
                                    xtype: 'displayfield',
                                    value: logDateString
                                },
                                {
                                    fieldLabel: '&nbsp;',
                                    flex: 4,
                                    labelWidth: 0,
                                    xtype: 'displayfield',
                                    value: Ext.String.format(Uni.I18n.translate('dbp.process.nodeDescription', 'DBP', '{0} ({1})'), nodeName, nodeType)
                                }]
                        }
                    ));
                });
                me.getTaskNodesForm().removeAll();
                me.getTaskNodesForm().items = formItems;
                me.getTaskNodesForm().doLayout();

                //previewStatusProcess.setLoading();


            }
        })

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