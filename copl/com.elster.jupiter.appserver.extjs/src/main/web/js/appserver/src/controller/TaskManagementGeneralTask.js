/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.TaskManagementGeneralTask', {
    extend: 'Ext.app.Controller',
    stores: [],
    models: [
        'Apr.model.Triggers'
    ],
    views: [
        'Apr.view.taskmanagement.DetailsGeneralTask'
    ],
    refs: [
        {
            ref: 'detailsPage',
            selector: 'general-task-details'
        }
    ],

    canAdministrate: function () {
        return false;
    },

    canView: function () {
        return false;
    },

    canRun: function () {
        return false;
    },

    canEdit: function () {
        return false;
    },

    canSetTriggers: function () {
        return false;
    },

    canHistory: function () {
        return false;
    },

    getType: function () {
        return '##generalTask##';
    },

    canRemove: function () {
        return false;
    },

    getTaskForm: function (caller, completedFunc) {
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;

        me.getModel('Apr.model.Triggers').load(taskManagementId, {
            success: function (record) {
                operationCompleted.call(controller, me, taskManagementId, record);
            }
        });
    },

    viewTaskManagement: function (taskId, actionMenu, taskManagementRecord) {
        var me = this,
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('general-task-details',{
                actionMenu: actionMenu
            }),
            recurrentTask = taskManagementRecord.get('recurrentTask');

        pageMainContent.setLoading(true);
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getApplication().fireEvent('loadTask', recurrentTask.name);
        widget.down('#general-task-details-side-menu').setHeader(recurrentTask.name);
        widget.down('#name-field-container').setValue(recurrentTask.name);
        widget.setRecurrentTasks('#followedBy-field-container', taskManagementRecord.get('nextRecurrentTasks'));
        widget.setRecurrentTasks('#precededBy-field-container', taskManagementRecord.get('previousRecurrentTasks'));
        if (actionMenu){
            me.actionMenu = actionMenu;
            var actionMenuItem = me.getDetailsPage().down('#' + actionMenu.itemId)
            actionMenuItem.record = taskManagementRecord.getRecurrentTask();
            actionMenuItem.setVisible(true);
        }
        pageMainContent.setLoading(false);
    }
});
