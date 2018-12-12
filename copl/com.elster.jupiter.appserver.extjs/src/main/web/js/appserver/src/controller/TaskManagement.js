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
        'Apr.store.QueuesByApplication',
        'Apr.store.CustomTaskTypes'
    ],
    models: [
        'Apr.model.Task',
        'Apr.model.Queue',
        'Apr.model.TaskInfo',
        'Apr.model.Triggers',
        'Apr.model.CustomTaskType'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'task-management-setup'
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
                select: this.showPreview
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
            }
        });
    },

    showTaskManagement: function () {
        var me = this,
            queuesStore = me.getStore('Apr.store.QueuesByApplication'), widget;

        queuesStore.getProxy().extraParams = {application: this.applicationKey};
            widget = Ext.widget('task-management-setup', {
                applicationKey: me.applicationKey,
                addTaskRoute: me.addTaskRoute,
                queuesStore: queuesStore,
                router: me.getController('Uni.controller.history.Router')
            });
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    viewTaskManagement: function (taskType, taskManagementId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        if ((taskManagement && taskManagement.controller && taskManagement.controller.canView()) ||
            (taskManagement && taskManagement.controller && taskManagement.controller.getTaskRoute)) {

            taskManagement.controller.getTask(this, taskManagementId, function (taskController, taskManagementId, task) {
                var route = '';
                if (taskManagement.controller.getTaskRoute) {
                    route = me.getController('Uni.controller.history.Router').getRoute(taskManagement.controller.getTaskRoute());
                    window.location.replace(route.buildUrl({taskId: task.get('id')}));
                }
                else {
                    route = me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement/view');
                    window.location.replace(route.buildUrl({type: taskType, taskManagementId: taskManagementId, taskId: task.get('id')}));
                }
            })
        }
        else {
            var generalTask = me.getController('Apr.controller.TaskManagementGeneralTask')
            generalTask.getTask(this, taskManagementId, function (taskController, taskManagementId, task) {
                generalTask.viewTaskManagement(taskManagementId, null, task);
            })
        }
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

        taskPreview.setLoading();
        me.getModel('Apr.model.Triggers').load(record.get('id'), {
            success: function (triggers) {
                taskPreview.setRecurrentTasks('#followedBy-field-container', triggers.get('nextRecurrentTasks'));
                taskPreview.setRecurrentTasks('#precededBy-field-container', triggers.get('previousRecurrentTasks'));
                taskPreview.setLoading(false);
            }
        });
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
                taskManagement && taskManagement.controller && taskManagement.controller.getTask(this, record.get('id'), me.viewDetailsTaskLoaded);
                break;
            case 'historyTask':
                taskManagement && taskManagement.controller && taskManagement.controller.getTask(this, record.get('id'), me.viewHistoryTaskLoaded)
                break;
            case 'removeTask':
                taskManagement && taskManagement.controller && taskManagement.controller.removeTaskManagement(record, me.removeOperationStart, me.removeOperationCompleted, this);
                break;
            case 'setTriggers':
                me.showSetTriggers(record);
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
        Apr.TaskManagementApp.getTaskManagementApps().get(newValue).controller.getTaskForm(this, function (form) {
            me.getAddPage().down('#task-management-attributes').add(form);
            Ext.resumeLayouts(true);
        });
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
        Apr.TaskManagementApp.getTaskManagementApps().get(taskType).controller.getTaskForm(this, function (form) {
            view.down('#task-management-attributes').add(form);
            me.getApplication().fireEvent('changecontentevent', view);
            taskManagement.controller.editTaskManagement(taskId, view.down('#form-errors'),
                me.editOperationStart, me.editOperationCompleteLoading, me.editOperationCompleted, me.editSetTitle, this);
        });
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
        me.removeOperationCompleted(status);

    },

    removeOperationStart: function () {
    },

    removeOperationCompleted: function () {
        var me = this;

        if (status && me.getPage()) {
            var grid = me.getTaskManagementGrid();
            grid.down('pagingtoolbartop').totalCount = 0;
            grid.down('pagingtoolbarbottom').resetPaging();
            grid.getStore().load();
        } else {
            me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement').forward();
        }
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

        if (!taskManagement && Apr.TaskManagementApp.dependencesCounter != 0) {
            var route = me.getController('Uni.controller.history.Router');
            window.location.replace(route.getRoute().buildUrl(route.arguments));
            return;
        }
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
