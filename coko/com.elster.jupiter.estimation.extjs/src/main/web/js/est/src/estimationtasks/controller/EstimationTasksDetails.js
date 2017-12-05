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
        {ref: 'actionMenu', selector: 'estimationtasks-action-menu'},
        {ref: 'detailsView', selector: 'estimationtasks-details'}
    ],

    init: function () {
    },

    detailRoute: 'administration/estimationtasks/estimationtask',
    historyRoute: 'administration/estimationtasks/estimationtask/history',
    actionMenu: {
        xtype: 'estimationtasks-action-menu',
        itemId: 'estimationtasks-action-menu'
    },

    showEstimationTaskDetails: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Est.estimationtasks.model.EstimationTask'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('estimationtasks-details', {
                router: router,
                taskId: currentTaskId,
                detailRoute: me.detailRoute,
                historyRoute: me.historyRoute,
                actionMenu: me.actionMenu
            });
        me.getController('Est.estimationtasks.controller.EstimationTasksAddEdit').fromDetails = true;

        pageMainContent.setLoading(true);

        taskModel.load(currentTaskId, {
            success: function (record) {
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('estimationTaskLoaded', record);
                me.getApplication().fireEvent('loadTask', record);
                widget.down('#estimationtasks-side-menu').setHeader(record.get('name'));
                me.getDetailForm().getForm().loadRecord(record);
                me.getDetailForm().setRecurrentTasks('#followedBy-field-container', record.get('nextRecurrentTasks'));
                me.getDetailForm().setRecurrentTasks('#precededBy-field-container', record.get('previousRecurrentTasks'));
                me.getActionMenu() && (me.getActionMenu().record = record);
                if (widget.down('#run-estimation-task') && record.get('status') !== 'Busy') {
                    if (Est.privileges.EstimationConfiguration.canRun()) {
                        widget.down('#run-estimation-task').show();
                    }
                } else if (widget.down('#run-estimation-task')) {
                    widget.down('#run-estimation-task').hide();
                }
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    }
});