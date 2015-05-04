Ext.define('Cfg.controller.Log', {
    extend: 'Ext.app.Controller',
    views: [
        'Cfg.view.log.Setup'
    ],
    stores: [
        'Cfg.store.Logs',
        'Cfg.store.ValidationTasksHistory'
    ],
    models: [
        'Cfg.model.Log',
        'Cfg.model.ValidationTask',
        'Cfg.model.ValidationTaskHistory'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'cfg-log-setup'
        }
    ],

    showLog: function (taskId, occurrenceId) {
        var me = this,
            taskModel = me.getModel('Cfg.model.ValidationTask'),
            logsStore = me.getStore('Cfg.store.Logs'),
            historyStore = me.getStore('Cfg.store.ValidationTasksHistory'),
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
                    view = Ext.widget('cfg-log-setup', {
                        router: router,
                        task: record,
                        runStartedOn: runStartedOnFormatted
                    });
                    sideMenu = view.down('#log-view-menu');
                    me.getApplication().fireEvent('validationtaskload', record);
                    sideMenu.setTitle(record.get('name'));
                    view.down('#log-preview-form').loadRecord(occurrenceTask);
                    view.down('#run-started-on').setValue(runStartedOnFormatted);
                    me.getApplication().fireEvent('changecontentevent', view);
                });
            }
			
        });
    }
});
