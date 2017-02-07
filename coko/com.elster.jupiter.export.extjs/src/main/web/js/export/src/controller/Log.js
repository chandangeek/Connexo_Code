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

    requires: [
        'Uni.util.LogLevel'
    ],

    //init: function () {
        //Uni.util.LogLevel.loadLogLevels();
        //this.callParent(arguments);
    //},

    showLog: function (taskId, occurrenceId) {
        var me = this,
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            logsStore = me.getStore('Dxp.store.Logs'),
            historyStore = me.getStore('Dxp.store.DataExportTasksHistory'),
            router = me.getController('Uni.controller.history.Router'),
            view,
            runStartedOnFormatted,
            taskLink,
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
                    runStartedOnFormatted = occurrenceTask.data.startedOn_formatted;
                    view = Ext.widget('export-log-setup', {
                        router: router,
                        task: record,
                        runStartedOn: runStartedOnFormatted
                    });
                    taskLink = view.down('#log-view-menu #tasks-view-link');
                    taskLink.setText(record.get('name'));
                    me.getApplication().fireEvent('dataexporttaskload', record);
                    view.down('#log-preview-form').loadRecord(occurrenceTask);
                    view.down('#reason-field').setVisible(occurrenceTask.get('status')==='Failed');
                    view.down('#run-started-on').setValue(runStartedOnFormatted);
                    view.down('#des-log-preview').updateSummary(occurrenceTask.get('summary'));
                    me.getApplication().fireEvent('changecontentevent', view);
                });
            }
        });
    }
});
