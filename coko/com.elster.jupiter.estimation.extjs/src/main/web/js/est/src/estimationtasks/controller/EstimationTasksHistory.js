Ext.define('Est.estimationtasks.controller.EstimationTasksHistory', {
    extend: 'Ext.app.Controller',

    requires: [],

    stores: [
        'Est.estimationtasks.store.EstimationTasksHistory'
    ],

    models: [
        'Est.estimationtasks.model.HistoryFilter'
    ],

    views: [
        'Est.estimationtasks.view.History'
    ],

    refs: [
        {ref: 'overviewLink', selector: '#estimationtasks-overview-link'},
        {ref: 'history', selector: 'estimationtasks-history'}
    ],

    init: function () {
        this.control({
            'estimationtasks-history-grid': {
                select: this.showPreview
            }
        });
    },

    showEstimationTaskHistory: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Est.estimationtasks.model.EstimationTask'),
            store = me.getStore('Est.estimationtasks.store.EstimationTasksHistory'),
            widget,
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        store.getProxy().setUrl(router.arguments);

        pageMainContent.setLoading(true);

        taskModel.load(currentTaskId, {
            success: function (record) {
                widget = Ext.widget('estimationtasks-history', {router: router, taskId: currentTaskId});
                me.getApplication().fireEvent('changecontentevent', widget);
                Ext.suspendLayouts();
                me.getOverviewLink().setText(record.get('name'));
                me.getApplication().fireEvent('estimationTaskLoaded', record);
                Ext.resumeLayouts(true);
            },
            callback: function(){
                pageMainContent.setLoading(false);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getHistory(),
            preview = page.down('estimationtasks-history-preview'),
            previewForm = page.down('estimationtasks-history-preview-form');

        if (record) {
            Ext.suspendLayouts();
            preview.setTitle(record.get('startedOn_formatted'));
            previewForm.down('displayfield[name=startedOn_formatted]').setVisible(true);
            previewForm.down('displayfield[name=finishedOn_formatted]').setVisible(true);
            previewForm.loadRecord(record);
            preview.down('estimationtasks-history-action-menu').record = record;

            previewForm.loadRecord(record);
            Ext.resumeLayouts(true);
        }
    }
});
