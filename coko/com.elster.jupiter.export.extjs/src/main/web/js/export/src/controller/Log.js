Ext.define('Dxp.controller.Log', {
    extend: 'Ext.app.Controller',
    views: [
        'Dxp.view.log.Setup'
    ],
    stores: [
        'Dxp.store.Logs',
        'Dxp.store.DataExportTasksHistory'
    ],
    models: [
        'Dxp.model.Log',
        'Dxp.model.DataExportTask',
        'Dxp.model.DataExportTaskHistory'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'log-setup'
        }
    ],

    showLog: function (taskId, occurrenceId) {
        var me = this,
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            logsStore = me.getStore('Dxp.store.Logs'),
            historyStore = me.getStore('Dxp.store.DataExportTasksHistory'),
            router = me.getController('Uni.controller.history.Router'),
            view,
            runStartedOn,
            tasksSideMenu,
            occurrenceTask;

        historyStore.getProxy().setUrl(router.arguments);
        logsStore.getProxy().setUrl(router.arguments);
        taskModel.load(taskId, {
            success: function (record) {
                historyStore.load(function(records) {
                    records.map(function(r){
                        r.set(Ext.apply({}, r.raw, record.raw));
                    });
                    occurrenceTask = this.getById(parseInt(occurrenceId));
                    runStartedOn = moment(occurrenceTask.startedOn).valueOf();
                    view = Ext.widget('log-setup', {
                        router: router,
                        task: record
                    });
                    tasksSideMenu = view.down('#tasks-view-menu');
                    me.getApplication().fireEvent('dataexporttaskload', record);
                    tasksSideMenu.setTitle(record.get('name'));
                    tasksSideMenu.down('#tasks-log-link').show();
                    view.down('#log-preview-form').loadRecord(occurrenceTask);
                    view.down('#run-started-on').setValue(moment(runStartedOn).format('ddd, DD MMM YYYY HH:mm:ss'));
                    me.getApplication().fireEvent('changecontentevent', view);
                });
            }
        });
    }
});
