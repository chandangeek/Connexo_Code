Ext.define('Est.estimationtasks.controller.EstimationTasksOverview', {
    extend: 'Ext.app.Controller',

    requires: [],

    stores: [
        'Est.estimationtasks.store.EstimationTasks'
    ],

    views: [
        'Est.estimationtasks.view.Overview'
    ],

    refs: [
        {ref: 'actionMenu', selector: 'estimationtasks-action-menu'},
        {ref: 'preview', selector: 'estimationtasks-preview'},
        {ref: 'previewForm', selector: 'estimationtasks-detail-form'}
    ],

    init: function () {
        this.control({
            'estimationtasks-grid': {
                select: this.showPreview
            }
        });
    },

    showEstimationTasksOverview: function () {
        var me = this, widget = Ext.widget('estimationtasks-overview', {router: me.getController('Uni.controller.history.Router')});
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (selectionModel, record) {
        var me = this;

        Ext.Array.each(Ext.ComponentQuery.query('#run-estimation-task'), function (item) {
            item.setVisible(record.get('status') !== 'Busy');
        });

        Ext.suspendLayouts();

        me.getPreview().setTitle(record.get('name'));
        me.getPreviewForm().loadRecord(record);
        me.getActionMenu().record = record;

        Ext.resumeLayouts();
    }
});
