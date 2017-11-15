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
            selector: 'task-management-preview'
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
                queuesStore: queuesStore
            });
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (records, record) {
        var me = this,
            taskPreview = me.getTaskPreview();
        taskPreview.setTitle(Ext.String.htmlEncode(record.get('name')));
        taskPreview.down('form').loadRecord(record);
        taskPreview.down('task-management-action-menu').record = record;
        if (record.get('queueStatus') == 'Busy') {
            taskPreview.down('#durationField').show();
            taskPreview.down('#currentRunField').show();
        } else {
            taskPreview.down('#durationField').hide();
            taskPreview.down('#currentRunField').hide();
        }
    },

    showAddTask: function () {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            view = Ext.create('Apr.view.taskmanagement.Add', {
                edit: false,
                //addReturnLink: me.getController('Uni.controller.history.Router').getRoute(me.rootRoute).buildUrl(me.rootRouteArguments),
                addReturnLink: me.rootRouteWithArguments,
                storeTypes: me.getTypesStore()
            });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    getTypesStore: function () {
        var apps = Apr.TaskManagementApp.getTaskManagementApps(),
            data = [];

        apps.each(function (key, value, length) {
            data.push([key, value.name]);
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

    chooseMenuAction: function (menu, item) {
        var me = this,
            record = menu.record || me.getTaskManagementGrid().getSelectionModel().getLastSelected(),
            taskType = record.get('queue'),
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        switch (item.action) {
            case 'runTask':
                taskManagement && taskManagement.controller && taskManagement.controller.runTaskManagement(record, me.operationStart, me.operationCompleted, this);
                break;
            case 'editTask':
                taskManagement && taskManagement.controller && taskManagement.controller.editTaskManagement(record);
                break;
            case 'historyTask':
                taskManagement && taskManagement.controller && taskManagement.controller.historyTaskManagement(record);
                break;
            case 'removeTask':
                taskManagement && taskManagement.controller && taskManagement.controller.removeTaskManagement(record, me.operationStart, me.operationCompleted, this);
                break;
        }
    },

    onMenuShow: function (menu) {
        var taskType = menu.record.get('queue'),
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        menu.down('#run-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canRun());
        menu.down('#edit-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canEdit());
        menu.down('#history-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canHistory());
        menu.down('#remove-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canRemove());
        menu.reorderItems();
    },

    saveOperationComplete: function () {
        var me = this;

        me.getController('Uni.controller.history.Router').getRoute(me.rootRoute).forward(null, me.rootRouteArguments);
    },

    operationStart: function () {
        var me = this;

        me.getPage().setLoading(true);
    },

    operationCompleted: function (status) {
        var me = this;

        me.getPage().setLoading(false);
        if (status && me.getPage()) {
            var grid = me.getTaskManagementGrid();
            grid.down('pagingtoolbartop').totalCount = 0;
            grid.down('pagingtoolbarbottom').resetPaging();
            grid.getStore().load();
        } //else {
        //  me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks').forward();
        //  }
    }
});
