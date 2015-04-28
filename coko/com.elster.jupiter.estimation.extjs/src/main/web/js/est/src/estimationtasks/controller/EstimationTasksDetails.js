Ext.define('Est.estimationtasks.controller.EstimationTasksDetails', {
    extend: 'Ext.app.Controller',

    requires: [],

    stores: [],

    views: [
        'Est.estimationtasks.view.Details'
    ],

    refs: [
        {ref: 'overviewLink', selector: '#estimationtasks-overview-link'},
        {ref: 'detailForm', selector: 'estimationtasks-detail-form'},
        {ref: 'actionMenu', selector: 'estimationtasks-action-menu'}
    ],

    init: function () {
    },

    showEstimationTaskDetails: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Est.estimationtasks.model.EstimationTask'),
            widget = Ext.widget('estimationtasks-details', {router: router, taskId: currentTaskId});
        me.getController('Est.estimationtasks.controller.EstimationTasksAddEdit').fromDetails = true;
        me.getApplication().fireEvent('changecontentevent', widget);

        widget.setLoading(true);

        taskModel.load(currentTaskId, {
            success: function (record) {
                me.getApplication().fireEvent('estimationTaskLoaded', record);
                me.getOverviewLink().setText(record.get('name'));
                me.getDetailForm().getForm().loadRecord(record);
                me.getActionMenu().record = record;
                me.getActionMenu().down('#run-estimation-task').setVisible(record.get('status') !== 'Busy');
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
    }
});