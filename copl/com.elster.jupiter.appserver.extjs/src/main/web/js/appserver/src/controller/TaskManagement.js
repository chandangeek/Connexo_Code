/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.TaskManagement', {
    extend: 'Ext.app.Controller',

    views: [
        'Apr.view.taskmanagement.Setup',
        'Apr.view.taskmanagement.TaskGrid',
        'Apr.view.taskmanagement.TaskPreview',
        'Apr.view.taskmanagement.TaskFilter',
        'Apr.view.taskmanagement.Add'
    ],
    stores: [
        'Apr.store.Tasks',
        'Apr.store.QueuesByApplication'
    ],
    models: [
        'Apr.model.Task',
        'Apr.model.Queue'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'task-management-setup',
        },
        {
            ref: 'taskPreview',
            selector: 'task-management-setup #task-management-preview'
        },
        {
            ref: 'filter',
            selector: 'task-management-filter'
        },
        {
            ref: 'addPage',
            selector: 'task-management-add'
        },
        {
            ref: 'taskManagementGrid',
            selector: 'task-management-grid'
        }
    ],

    init: function () {
        this.control({
            'task-management-grid': {
                select: this.showPreview,
                cellclick: this.cellclick
            },
            'task-management-add combobox[itemId=task-management-task-type]': {
                change: this.taskTypeChanged
            },
            'task-management-add #add-button': {
                click: this.addTask
            },
            'task-management-action-menu': {
                click: this.chooseMenuAction,
                show: this.onMenuShow
            },
        });
    },

    showTaskManagement: function () {
        var me = this,
            queuesStore = me.getStore('Apr.store.QueuesByApplication');

        queuesStore.getProxy().extraParams = {application: this.applicationKey};
            widget = Ext.widget('task-management-setup', {
                applicationKey: me.applicationKey,
                addTaskRoute: me.addTaskRoute,
                queuesStore: queuesStore,
                router: me.getController('Uni.controller.history.Router')
            });
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (records, record) {
        var me = this,
            taskPreview = me.getTaskPreview(),
            taskType = record.get('queue'),
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        Ext.suspendLayouts();
        taskPreview.setTitle(Ext.String.htmlEncode(record.get('name')));
        taskPreview.down('#task-management-preview-form').loadRecord(record);
        taskPreview.down('#btn-task-management-preview-action-menu').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canAdministrate());
        taskPreview.down('task-management-action-menu').record = record;
        if (record.get('queueStatus') == 'Busy') {
            taskPreview.down('#durationField').show();
            taskPreview.down('#currentRunField').show();
        } else {
            taskPreview.down('#durationField').hide();
            taskPreview.down('#currentRunField').hide();
        }
        Ext.resumeLayouts(true);
    },

    cellclick: function (table, td, cellIndex, record) {
        if (cellIndex == 0) {
            var me = this,
                taskType = record.get('queue'),
                taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);
            taskManagement && taskManagement.controller &&
            taskManagement.controller.getTask(this, record.get('id'), function (taskController, taskManagementId, task) {
                var route = me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement/view');
                route.forward({type: taskType, taskManagementId: taskManagementId, taskId: task.get('id')});
            })
        }
    },

    chooseMenuAction: function (menu, item) {
        var me = this,
            record = menu.record || me.getTaskManagementGrid().getSelectionModel().getLastSelected(),
            taskType = record.get('queue'),
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType),
            route;

        switch (item.action) {
            case 'runTask':
                taskManagement && taskManagement.controller && taskManagement.controller.runTaskManagement(record, me.operationStart, me.operationCompleted, this);
                break;
            case 'editTask':
                taskManagement && taskManagement.controller && taskManagement.controller.getTask(this, record.get('id'), me.viewDetailsTaskLoaded)
                //route = me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement/view/edit');
                //taskManagement && taskManagement.controller && route.forward({type: taskType, taskManagementId: record.get('id')});
                break;
            case 'historyTask':
                //route = me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement/history');
                taskManagement && taskManagement.controller && taskManagement.controller.getTask(this, record.get('id'), me.viewHistoryTaskLoaded)
                //taskManagement && taskManagement.controller && route.forward({type: taskType, taskId: record.get('id')});
                break;
            case 'removeTask':
                taskManagement && taskManagement.controller && taskManagement.controller.removeTaskManagement(record, me.operationStart, me.operationCompleted, this);
                break;
        }
    },

    onMenuShow: function (menu) {
        var taskType = menu.record.get('queue'),
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        Ext.suspendLayouts();
        menu.down('#run-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canRun());
        menu.down('#edit-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canEdit());
        menu.down('#history-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canHistory());
        menu.down('#remove-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canRemove());
        menu.reorderItems();
        Ext.resumeLayouts(true);
    },

    /* add task section */
    showAddTask: function () {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            view = Ext.create('Apr.view.taskmanagement.Add', {
                edit: false,
                addReturnLink: me.rootRouteWithArguments,
                storeTypes: me.getTypesStore()
            });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    getTypesStore: function () {
        var apps = Apr.TaskManagementApp.getTaskManagementApps(),
            data = [];

        apps.each(function (key, value, length) {
            value.controller && value.controller.canAdministrate() && data.push([key, value.name]);
        });

        return new Ext.data.ArrayStore({
            fields: ['id', 'name'],
            data: data,
            sorters: [
                {
                    property: 'name',
                    direction: 'ASC'
                }
            ]
        });
    },

    taskTypeChanged: function (field, newValue) {
        var me = this;

        if (field.suspendComboChange) {
            return;
        }

        Ext.suspendLayouts();
        me.getAddPage().down('#add-button').setDisabled(false);
        me.getAddPage().down('#task-management-attributes').removeAll();
        me.getAddPage().down('#task-management-attributes').add(Apr.TaskManagementApp.getTaskManagementApps().get(newValue).controller.getTaskForm());
        Ext.resumeLayouts(true);
    },

    addTask: function (button) {
        var me = this,
            taskType = me.getAddPage().down('#task-management-task-type').getValue();

        var saveStatus = Apr.TaskManagementApp.getTaskManagementApps().get(taskType).controller.saveTaskForm(
            me.getAddPage().down('#task-management-attributes'),
            me.getAddPage().down('#form-errors'),
            me.saveOperationComplete,
            this
        );
    },

    /* edit task section */
    editTask: function (taskType, taskId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType),
            appName = Uni.util.Application.getAppName(),
            view = Ext.create('Apr.view.taskmanagement.Add', {
                edit: true,
                addReturnLink: me.rootRouteWithArguments,
                storeTypes: me.getTypesStore()
            });

        view.down('#task-management-task-type').setDisabled(true);
        view.down('#add-button').setDisabled(false);
        view.down('#task-management-task-type').suspendComboChange = true;
        view.down('#task-management-task-type').setValue(taskType);
        view.down('#task-management-task-type').suspendComboChange = false;
        view.down('#task-management-attributes').removeAll();
        view.down('#task-management-attributes').add(Apr.TaskManagementApp.getTaskManagementApps().get(taskType).controller.getTaskForm());

        me.getApplication().fireEvent('changecontentevent', view);
        taskManagement.controller.editTaskManagement(taskId, view.down('#form-errors'),
            me.editOperationStart, me.editOperationCompleteLoading, me.editOperationCompleted, me.editSetTitle, this);
    },

    editSetTitle: function (taskName) {
        var me = this;

        me.getAddPage().down('#frm-add-task').setTitle(Uni.I18n.translate('general.editx', 'APR', "Edit '{0}'", taskName, false));
        me.getApplication().fireEvent('loadTask', taskName);
    },

    editOperationStart: function () {
        this.getAddPage().setLoading(true);
    },

    editOperationCompleteLoading: function () {
        this.getAddPage().setLoading(false);
    },

    editOperationCompleted: function (status) {
        var me = this;
        me.getController('Uni.controller.history.Router').getRoute(me.rootRoute).forward(null, me.rootRouteArguments);
    },

    /* common section */
    saveOperationComplete: function () {
        var me = this;
        me.getController('Uni.controller.history.Router').getRoute(me.rootRoute).forward(null, me.rootRouteArguments);
    },

    operationStart: function () {
        var me = this;
        me.getPage() && me.getPage().setLoading(true);
    },

    operationCompleted: function (status) {
        var me = this;

        me.getPage() && me.getPage().setLoading(false);
        if (status && me.getPage()) {
            var grid = me.getTaskManagementGrid();
            grid.down('pagingtoolbartop').totalCount = 0;
            grid.down('pagingtoolbarbottom').resetPaging();
            grid.getStore().load();
        } //else {
        //  me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks').forward();
        //  }
    },

    /* view history task section */
    viewHistoryTaskLoaded: function (taskController, taskManagementId, task) {
        var me = this,
            route = me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement/view/history');

        route.forward({type: taskController.getType(), taskManagementId: taskManagementId, taskId: task.get('id')});
    },

    viewHistoryTask: function (taskType, taskManagementId, taskId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        taskManagement.controller.detailRoute = 'administration/taskmanagement/view';
        taskManagement.controller.historyRoute = 'administration/taskmanagement/view/history';
        taskManagement.controller.viewLogRoute = 'administration/taskmanagement/view/history/occurrence';
        taskManagement.controller.historyTaskManagement(taskId);
    },

    /* view history task section */
    viewHistoryTaskLog: function (taskType, taskManagementId, taskId, occurrenceId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        taskManagement.controller.detailLogRoute = 'administration/taskmanagement/view';
        taskManagement.controller.logRoute = 'administration/taskmanagement/view/history/occurrence';
        taskManagement.controller.historyLogTaskManagement(taskId, occurrenceId);
    },


    /* view task section*/
    viewDetailsTaskLoaded: function (taskController, taskManagementId, task) {
        var me = this,
            route = me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement/view/edit');

        route.forward({type: taskController.getType(), taskManagementId: taskManagementId, taskId: task.get('id')});
    },

    viewTask: function (taskType, taskManagementId, taskId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        me.getModel('Apr.model.Task').load(taskManagementId, {
            success: function (record) {
                taskManagement.controller.detailRoute = 'administration/taskmanagement/view';
                taskManagement.controller.historyRoute = 'administration/taskmanagement/view/history';
                taskManagement.controller.viewTaskManagement(taskId, {
                    xtype: 'task-management-action-menu',
                    itemId: 'task-management-action-menu'
                }, record);
            }
        });

    }

});
