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
        'Apr.view.taskoverview.SetQueue'
    ],
    stores: [
        'Apr.store.Tasks',
        'Apr.store.Applications',
        'Apr.store.Queues',
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
        }

    ],


    init: function () {
        this.control({
            'task-overview-grid':{
                select: this.showPreview
            },
            'task-overview-action-menu': {
                click: this.chooseAction
            },
        });
    },

    showTaskOverview: function () {
        var me = this;
        me.getApplication().fireEvent('changecontentevent', Ext.widget('task-overview-setup'));
    },

    showPreview: function(records,record){
        var me = this;
        this.getTaskPreview().setTitle(Ext.String.htmlEncode(record.get('name')));
        this.getTaskPreview().down('form').loadRecord(record);
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
            case 'setQueue':
                me.setQueue(record);
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

    setQueue: function (record) {

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            itemId: 'setQueueConfirmationWindow',
            confirmText: Uni.I18n.translate('general.save', 'APR', 'Save'),
            closeAction: 'destroy',
            green: true,
            record: record,
            confirmation: Ext.bind(this.onCheck, this, [getConfirmationWindow])
        });

        confirmationWindow.insert(1, {
            xtype: 'set-queue',
            itemId: 'setQueueItem',
            padding: '-10 0 0 -20',
            record: record
        });

        confirmationWindow.show({
            title: Uni.I18n.translate('general.setqueue', 'APR', "Set queue for '{0}'?", record.data.name, false)
        });

        function getConfirmationWindow() {
            return confirmationWindow;
        }
    }
});
