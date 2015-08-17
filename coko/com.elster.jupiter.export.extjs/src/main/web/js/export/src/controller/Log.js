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
                    //runStartedOn = moment(occurrenceTask.startedOn).valueOf();
                    runStartedOnFormatted = occurrenceTask.data.startedOn_formatted;
                    view = Ext.widget('log-setup', {
                        router: router,
                        task: record,
                        runStartedOn: runStartedOnFormatted
                    });
                    sideMenu = view.down('#log-view-menu');
                    me.getApplication().fireEvent('dataexporttaskload', record);
                    sideMenu.setTitle(record.get('name'));
                    view.down('#log-preview-form').loadRecord(occurrenceTask);
                    view.down('#reason-field').setVisible(occurrenceTask.get('status')==='Failed');
                    view.down('#run-started-on').setValue(runStartedOnFormatted);
                    me.getApplication().fireEvent('changecontentevent', view);
                });
            }
        });
    }
});
