/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    showLogWorkspace: function (occurrenceId) {
        this.showLog(null, occurrenceId, true);
    },

    showLog: function (taskId, occurrenceId, fromWorkspace) {
        var me = this,
            taskModel = me.getModel('Dxp.model.DataExportTaskHistory'),
            logsStore = me.getStore('Dxp.store.Logs'),
            router = me.getController('Uni.controller.history.Router'),
            view,
            runStartedOnFormatted,
            taskLink;


        logsStore.getProxy().setUrl(router.arguments);
        taskModel.load(occurrenceId, {
            success: function (occurrenceTask) {
                var task = occurrenceTask.getTask();
                    runStartedOnFormatted = occurrenceTask.data.startedOn_formatted;
                    view = Ext.widget('log-setup', {
                        router: router,
                        task: task,
                        runStartedOn: runStartedOnFormatted,
                        fromWorkspace: fromWorkspace
                    });
                Ext.suspendLayouts();
                if(!fromWorkspace){
                    taskLink = view.down('#log-view-menu #tasks-view-link');
                    taskLink.setText(task.get('name'));
                    me.getApplication().fireEvent('dataexporttaskload', task);
                } else {
                    me.getApplication().fireEvent('exporthistorylogload', occurrenceTask);
                    view.down('#main-panel').setTitle(
                        Uni.I18n.translate('exportTask.log.of.occurence', 'DES', "Log '{0}'", occurrenceTask.get('startedOn_formatted'), false)
                    );
                }
                view.down('#log-preview-form').loadRecord(occurrenceTask);
                view.down('#reason-field').setVisible(occurrenceTask.get('status') === 'Failed');
                view.down('#run-started-on').setValue(runStartedOnFormatted);
                view.down('#des-log-preview').updateSummary(occurrenceTask.get('summary'));
                me.getApplication().fireEvent('changecontentevent', view);
                Ext.resumeLayouts(true);
            }
        });
    }
});
