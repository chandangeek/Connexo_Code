/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.controller.TaskManagement', {
    extend: 'Ext.app.Controller',

    views: [],
    refs: [
        {
            ref: 'page',
            selector: 'data-export-tasks-add'
        }
    ],

    init: function () {
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.exportTasks', 'DES', 'Export tasks'),
            controller: this
        });
    },

    canAdministrate: function () {
        return Dxp.privileges.DataExport.canAdministrate();
    },

    canView: function () {
        return Dxp.privileges.DataExport.canView();
    },

    canRun: function () {
        return false;
    },

    canEdit: function () {
        return false;
    },

    canSetTriggers: function () {
        return Dxp.privileges.DataExport.canUpdateFull();
    },

    canHistory: function () {
        return false;
    },

    canRemove: function () {
        return false;
    },

    getTaskRoute: function () {
        return 'administration/dataexporttasks/dataexporttask';
    },

    getType: function () {
        return 'DataExport'
    },

    getTaskForm: function(caller, completedFunc){
         return this.getController('Dxp.controller.Tasks').appendAddTaskForm(caller, completedFunc);
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;
        Ext.Ajax.request({
            url: '/api/export/dataexporttask/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Dxp.store.DataExportTasks');
                store.loadRawData([response]);
                store.each(function (record) {
                    operationCompleted.call(controller, me, taskManagementId, record);
                });
            }
        })
    },
    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
        var me = this;
        me.getController('Dxp.controller.Tasks').addTask({action: 'addTask'}, formErrorsPanel, saveOperationComplete, controller);
    }

});
