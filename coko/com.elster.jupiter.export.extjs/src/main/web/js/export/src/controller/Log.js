Ext.define('Dxp.controller.Log', {
    extend: 'Ext.app.Controller',
    views: [
        'Dxp.view.log.Setup'
    ],
    stores: [
        'Dxp.store.Logs'
    ],
    models: [
        'Dxp.model.Log',
        'Dxp.model.DataExportTask'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'log-setup'
        }
    ],

    init: function () {
        this.control({

        });
    },

    showLog: function (taskId) {
        var me = this,
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            view,
            tasksSideMenu;

        taskModel.load(taskId, {
            success: function (record) {
                view = Ext.widget('log-setup', {
                    router: me.getController('Uni.controller.history.Router'),
                    task: record
                });
                tasksSideMenu = view.down('#tasks-view-menu');
                me.getApplication().fireEvent('dataexporttaskload', record);
                tasksSideMenu.setTitle(record.get('name'));
                tasksSideMenu.down('#tasks-log-link').show();
                view.down('#log-preview-form').loadRecord(record);
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });

    }
});
