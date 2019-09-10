/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.TaskOverview', {
    extend: 'Ext.app.Controller',
    views: [
        'Apr.view.taskoverview.Setup',
        'Apr.view.taskoverview.TaskOverviewGrid',
        'Apr.view.taskoverview.TaskPreviewForm',
        'Apr.view.taskoverview.TaskPreview',
        'Apr.view.taskoverview.TaskFilter',
        'Apr.view.taskoverview.QueueAndPriorityWindow'
    ],
    stores: [
        'Apr.store.Tasks',
        'Apr.store.Applications',
        'Apr.store.Queues',
        'Apr.store.SuspendedTask',
        'Apr.store.TasksType'
    ],
    models: [
        'Apr.model.Task',
        'Apr.model.Application',
        'Apr.model.Queue'
    ],
    refs: [
        {
            ref: 'taskPreview',
            selector: 'task-preview'
        },
        {
            ref: 'filter',
            selector: 'taskFilter'
        },
        {
            ref: 'taskOverviewGrid',
            selector: '#task-overview-grid'
        },
        {
            ref: 'queuePriorityWindow',
            selector: 'queue-priority-window-overview'
        }

    ],


    init: function () {
        this.control({
            'task-overview-grid':{
                select: this.showPreview
            },
            'task-overview-action-menu': {
                click: this.chooseAction,
                show: this.showMenu
            },
            'queue-priority-window-overview #save-queue-priority-button': {
                click: this.saveQueuePriority
            },
        });
    },

    showMenu: function (menu) {
        Ext.suspendLayouts();
        menu.down('#set-queue-priority').setVisible(Uni.Auth.checkPrivileges(Apr.privileges.AppServer.administrateTaskOverview)
                    && menu.record.get('extraQueueCreationEnabled') || menu.record.get('queuePrioritized'));
        menu.reorderItems();
        Ext.resumeLayouts(true);
    },
    showTaskOverview: function () {
        var me = this;
        me.getApplication().fireEvent('changecontentevent', Ext.widget('task-overview-setup'));
    },

    showPreview: function(records,record){
        var me = this;
        this.getTaskPreview().setTitle(Ext.String.htmlEncode(record.get('name')));
        this.getTaskPreview().down('form').loadRecord(record);
        this.getTaskPreview().down('task-overview-action-menu').record = record;
        if(record.get('queueStatus')== 'Busy'){
            this.getTaskPreview().down('#durationField').show();
            this.getTaskPreview().down('#currentRunField').show();
        } else {
            this.getTaskPreview().down('#durationField').hide();
            this.getTaskPreview().down('#currentRunField').hide();
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            record = menu.record || me.getTaskOverviewGrid().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'setQueueAndPriority':
                me.setQueueAndPriority(record);
                break;
        }
    },

    onCheck: function (getConfirmationWindow) {
        var me = this,
            confWindow = getConfirmationWindow();
        me.doOperation(confWindow, Uni.I18n.translate('general.task.set.on.queue', 'APR', 'Task set on queue'), 'setqueue');
    },

    doOperation: function (confirmationWindow, successMessage, action) {
        var me = this,
            router = me.router,
            updatedData;

        updatedData = {
            id: confirmationWindow.record.getId(),
            queue: confirmationWindow.down('#cmb-queue').getValue()
        };

        Ext.Ajax.request({
            url: '/api/tsk/task',
            method: 'PUT',
            jsonData: Ext.encode(updatedData),
            success: function (response) {
                confirmationWindow.close();
                me.getApplication().fireEvent('acknowledge', successMessage);
            },
            failure: function (response) {
                var json = Ext.decode(response.responseText, true);
                if (json && json.errors) {
                    confirmationWindow.down('#cmb-queue').markInvalid(json.errors[0].msg);

                }
            }
        });
    },
    setQueueAndPriority: function (record) {
            var me = this,
            store = Ext.getStore('Apr.store.TasksType');
            store.getProxy().setUrl(record.getId());
        store.load(function(records, operation, success) {
            if (success) {
                var window = Ext.widget('queue-priority-window-overview', {
                    record: record,
                    store: store
                });
                window.show();
            };
        });
    },

    saveQueuePriority: function() {
       var me = this,
            window = me.getQueuePriorityWindow(),
            record = window.record,
            taskId = record.getId(),
            priority = window.down('#priority-field').getValue(),
            queue = window.down('#queue-field').getValue(),
            updatedData;

        updatedData = {
            id: taskId,
            queue: queue,
            priority: priority
        };

        Ext.Ajax.request({
            url: '/api/tsk/task',
            method: 'PUT',
            jsonData: Ext.encode(updatedData),
            success: function (response) {
            record.set({
                'queue': queue,
                'priority': priority
            });
                window.close();
                me.getApplication().fireEvent('acknowledge', 'Task queue and priority changed.');
                me.getTaskPreview().down('form').loadRecord(record);
            },
        });
    },
});
