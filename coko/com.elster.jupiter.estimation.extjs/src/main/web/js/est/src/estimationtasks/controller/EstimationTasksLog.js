Ext.define('Est.estimationtasks.controller.EstimationTasksLog', {
    extend: 'Ext.app.Controller',
    views: [
        'Est.estimationtasks.view.Log'
    ],
    stores: [
        'Est.estimationtasks.store.Logs',
        'Est.estimationtasks.store.EstimationTasksHistory'
    ],
    models: [
        'Est.estimationtasks.model.Logs',
        'Est.estimationtasks.model.EstimationTask'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'estimationtasks-log-setup'
        }
    ],

    showLog: function (taskId, occurrenceId) {
        var me = this,
            taskModel = me.getModel('Est.estimationtasks.model.EstimationTask'),
            logsStore = me.getStore('Est.estimationtasks.store.Logs'),
            historyStore = me.getStore('Est.estimationtasks.store.EstimationTasksHistory'),
            router = me.getController('Uni.controller.history.Router'),
            widget,
            runStartedOnFormatted,
            sideMenu,
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
                    runStartedOnFormatted = occurrenceTask.get('startedOn_formatted');
                    widget = Ext.widget('estimationtasks-log-setup', {
                        router: router,
                        task: record,
                        runStartedOn: runStartedOnFormatted,
                        taskId: taskId,
                        occurenceId: occurrenceId
                    });
                    sideMenu = widget.down('#estimationtasks-log-menu');
                    me.getApplication().fireEvent('estimationTaskLoaded', record);
                    widget.down("#estimationtasks-overview-link").setText(record.get('name'));
                    widget.down('#estimationtasks-log-preview-form').loadRecord(occurrenceTask);
                    widget.down('#run-started-on').setValue(runStartedOnFormatted);
                    me.getApplication().fireEvent('changecontentevent', widget);
                });
            }

        });
    }
});
