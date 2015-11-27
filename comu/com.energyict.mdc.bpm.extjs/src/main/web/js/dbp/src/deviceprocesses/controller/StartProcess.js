Ext.define('Dbp.deviceprocesses.controller.StartProcess', {
    extend: 'Ext.app.Controller',
    requires: [],
    models: [
        'Dbp.deviceprocesses.model.HistoryProcessesFilter'
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
        'Dbp.deviceprocesses.view.DeviceProcessesMainView',
        'Dbp.deviceprocesses.view.StartProcess'
    ],
    refs: [
        {ref: 'mainPage', selector: 'dbp-device-processes-main-view'},
        {ref: 'processesTab', selector: '#tab-processes'},
        {ref: 'historyProcessesGrid', selector: '#history-processes-grid'},
        {ref: 'runningProcessesGrid', selector: '#running-processes-grid'},
        {ref: 'openTasksDisplay', selector: '#dbp-preview-running-process-open-tasks'},
        {ref: 'startProcess', selector: '#dbp-start-processes'}
    ],
    mRID: null,

    init: function () {
        var me = this;
        me.control({
            'dbp-running-processes #running-processes-grid': {
                select: this.showProcessPreview
            },
            'dbp-history-processes #history-processes-grid': {
                select: this.showHistoryPreview
            },
            '#tab-processes': {
                tabChange: this.changeTab
            }
        });
    },

    showStartProcess: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.mRID = mRID;
        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var widget = me.getStartProcess();

                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);

                //me.getStore('Dbp.deviceprocesses.store.RunningProcesses').getProxy().setUrl('mrid', me.mRID);
                //me.getStore('Dbp.deviceprocesses.store.HistoryProcesses').getProxy().setUrl('mrid', me.mRID);
                if (!widget) {
                    widget = Ext.widget('dbp-start-processes', {device: device});
                    me.getApplication().fireEvent('changecontentevent', widget);
                } else {
                    widget.device = device;
                }

                //var queryString = Uni.util.QueryString.getQueryStringValues(false);
                //me.getProcessesTab().setActiveTab(queryString.activeTab == 'history' ? 1 : 0);
/*
                me.getHistoryProcessesGrid().getStore().on('datachanged', function () {
                    me.getHistoryProcessesGrid().getSelectionModel().select(0);
                    return true;
                }, this);
*/
            },
            failure: function (response) {
                viewport.setLoading(false);
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
                if (!widget) {
                    widget = Ext.widget('dbp-device-processes-main-view', {device: device});
                    me.getApplication().fireEvent('changecontentevent', widget);
                } else {
                    widget.device = device;
                }

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

    showProcessPreview: function (selectionModel, record) {
        var me = this,
            mainPage = me.getMainPage(),
            preview = mainPage.down('dbp-running-process-preview'),
            previewForm = mainPage.down('#frm-preview-running-process'),
            router = me.getController('Uni.controller.history.Router'),
            openTasksValue = "";

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);

        record.openTasks().each(function (rec) {
            if (openTasksValue.length > 0) {
                openTasksValue += '<br>';
            }

            var taskName = rec.get('name').length >0? rec.get('name'): Uni.I18n.translate('dbp.process.noTaskName', 'DBP', 'No task name'),
                status = rec.get('statusDisplay'),
                assign = rec.get('actualOwner').length >0? rec.get('actualOwner'): Uni.I18n.translate('dbp.process.unassigned', 'DBP', 'Unassigned');

            openTasksValue += Ext.String.format('<a href =\"{0}\">{1}</a> ({2}, {3})',
                router.getRoute('workspace/taksmanagementtasks/openTask').buildUrl({taskId: rec.get('id')}),
                Ext.String.htmlEncode(taskName), status, assign);
        });

        me.getOpenTasksDisplay().setValue((openTasksValue.length > 0)? openTasksValue: Uni.I18n.translate('dbp.process.noOpenTasks', 'DBP', 'None'));
        Ext.resumeLayouts();
    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            mainPage = me.getMainPage(),
            preview = mainPage.down('dbp-history-process-preview'),
            previewForm = mainPage.down('#frm-preview-history-process');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        Ext.resumeLayouts();
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
            && me.getHistoryProcessesGrid().getStore().getCount() > 0
            && selectionModel.getCount() == 0) {
            selectionModel.select(0);
        }

        me.applyNewState(queryString);
    }
});