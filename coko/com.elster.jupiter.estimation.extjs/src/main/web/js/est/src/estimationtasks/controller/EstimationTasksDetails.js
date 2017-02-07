/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('estimationtasks-details', {router: router, taskId: currentTaskId});
        me.getController('Est.estimationtasks.controller.EstimationTasksAddEdit').fromDetails = true;

        pageMainContent.setLoading(true);

        taskModel.load(currentTaskId, {
            success: function (record) {
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('estimationTaskLoaded', record);
                me.getOverviewLink().setText(record.get('name'));
                me.getDetailForm().getForm().loadRecord(record);
                me.getActionMenu().record = record;
                widget.down('#estimationtasks-details-panel').setTitle(Ext.String.htmlEncode(record.get('name')));
                if (record.get('status') !== 'Busy') {
                    if (Est.privileges.EstimationConfiguration.canRun()) {
                        widget.down('#run-estimation-task').show();
                    }
                } else {
                    widget.down('#run-estimation-task').hide();
                }
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    }
});