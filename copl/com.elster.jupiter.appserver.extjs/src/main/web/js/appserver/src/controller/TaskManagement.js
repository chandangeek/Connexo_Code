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
        'Apr.store.Queues'
    ],
    models: [
        'Apr.model.Task',
        'Apr.model.Queue'
    ],
    refs: [
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
        });
    },

    showTaskManagement: function () {
        var me = this;

        me.getStore('Apr.store.Queues').getProxy().extraParams = {application: this.applicationKey};
        me.getApplication().fireEvent('changecontentevent', Ext.widget('task-management-setup', {
            applicationKey: me.applicationKey,
            addTaskRoute: me.addTaskRoute
        }));
    },

    showPreview: function (records, record) {
        var me = this,
            taskPreview = me.getTaskPreview();
        taskPreview.setTitle(Ext.String.htmlEncode(record.get('name')));
        taskPreview.down('form').loadRecord(record);
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
                addReturnLink: me.getController('Uni.controller.history.Router').getRoute(me.rootRoute).buildUrl(me.rootRouteArguments),
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
            me.getAddPage().down('#form-errors'));


    }
});
