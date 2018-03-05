/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.MessageQueues',{
    extend: 'Ext.app.Controller',
    requires: [
        'Apr.view.messagequeues.Menu',
        'Apr.view.messagequeues.MonitorSetup',
        'Apr.view.messagequeues.MonitorGrid',
        'Apr.view.messagequeues.MonitorPreview',
        'Apr.view.messagequeues.MonitorPreviewForm',
        'Apr.view.messagequeues.MonitorActionMenu',
        'Apr.view.messagequeues.Setup',
        'Apr.view.messagequeues.MessageQueuesGrid',
        'Apr.view.messagequeues.QueuePreview',
        'Apr.view.messagequeues.QueuePreviewForm',
        'Apr.view.messagequeues.QueueActionMenu',
        'Apr.view.messagequeues.AddMessageQueueForm'

    ],
    stores: [
        'Apr.store.MessageQueues',
        'Apr.store.MessageQueuesWithState',
        'Apr.store.QueuesType'
    ],

    refs: [
        {
            ref: 'preview',
            selector: '#monitor-preview'
        },
        {
            ref: 'queuePreview',
            selector: '#queue-preview'
        },
        {
            ref: 'saveButton',
            selector: '#save-message-queues-button'
        },
        {
            ref: 'undoButton',
            selector: '#undo-message-queues-changes-button'
        }
    ],

    init: function () {
        this.control({
            '#monitor-grid': {
                select: this.showPreview
            },
            'monitor-action-menu': {
                click: this.chooseAction
            },
            '#message-queues-grid': {
                edit: this.onEditMessageQueueGrid
            },
            '#undo-message-queues-changes-button':{
                click: this.onUndoChanges
            },
            '#save-message-queues-button':{
                click: this.onSaveChanges
            },
            'message-queue-setup message-queues-grid': {
                select: this.showQueuePreview
            },
            'queue-action-menu': {
                click: this.chooseAction
            },
            'add-queue-message-form #add-button': {
                click: this.addMessageQueue
            },
            'message-queue-setup #message-queues-grid': {
                queueRemoveEvent: this.removeMessageQueue
            }

        });
    },

    showMessageQueuesMonitor: function(){

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('monitor-setup', {
                router: router
            });
        me.getApplication().fireEvent('changecontentevent', view);
        this.getStore('Apr.store.MessageQueuesWithState').load(function(){
            // view.down('#message-queues-grid').select(0);
        });
    },

    addMessageQueue: function () {
        // var me = this,
        //     router = me.getController('Uni.controller.history.Router');
        alert('ADDDDDD');
    },

    removeMessageQueue: function () {
        alert('Remmmmoooveee');
    },


    showMessageQueues: function(){
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('message-queue-setup', {
                router: router
            });
        this.getStore('Apr.store.MessageQueues').load(function(){
            me.getApplication().fireEvent('changecontentevent', view);
            view.down('preview-container').updateOnChange(!me.getStore('Apr.store.MessageQueues').getCount());
        });
    },

    onEditMessageQueueGrid: function(){
        this.getSaveButton().enable();
        this.getUndoButton().enable();
    },

    onUndoChanges: function(){
        var me = this;
        this.getStore('Apr.store.MessageQueues').load(function(){
            me.getSaveButton().disable();
            me.getUndoButton().disable();
        });
    },

    onSaveChanges: function(){
        var me = this,
            records = this.getStore('Apr.store.MessageQueues').getModifiedRecords(),
            length = records.length;
        Ext.Array.each(this.getStore('Apr.store.MessageQueues').getModifiedRecords(),function(record){
            record.set('retryDelayInSeconds',record.get('retryDelayInMinutes')*60);
            delete record.data.retryDelayInMinutes;
            record.save({
                success: function () {
                   length--;
                   if(length === 0){
                       me.getStore('Apr.store.MessageQueues').load(function(){
                           me.getSaveButton().disable();
                           me.getUndoButton().disable();
                       });
                   }
                }
            });
        });
    },

    showPreview: function(selectionModel, record){
        var preview = this.getPreview();
        preview.setTitle(record.get('name'));
        preview.down('form').customLoadRecord(record);
        if (preview.down('monitor-action-menu')) {
            preview.down('monitor-action-menu').record = record;
        }
    },

    chooseAction: function(menu, item) {
        switch (item.action) {
            case 'clearErrorQueue':
                this.clearErrorQueue(menu.record);
                break;
            case 'remove':
                alert('remove');
                break;
        }
    },

    clearErrorQueue: function(record){
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        record.set('retryDelayInSeconds',record.get('retryDelayInMinutes')*60);
        delete record.data.retryDelayInMinutes;
        Ext.Ajax.request({
            url: '/api/msg/destinationspec/' + record.get('name') + '?purgeErrors=true',
            method: 'PUT',
            jsonData: record.getRecordData(),
            isNotEdit: true,
            success: function () {
                router.getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.queueCleared', 'APR', 'Error queue cleared'));
            }
        });
    },
    showQueuePreview: function (selectionModel, record) {
        var me = this,
            preview = me.getQueuePreview(),
            previewForm = preview.down('queue-preview-form'),
            tasksField = previewForm.down('[name=queueTasks]'),
            tasksList = [];


        Ext.suspendLayouts();
        preview.setTitle(Ext.htmlEncode(record.get('name')));
        previewForm.loadRecord(record);
        preview.down('queue-action-menu').record = record;
        preview.down('#queue-button-action').setVisible(!record.get('isDefault'));
        record.tasks().each(function (task) {
            tasksList.push('- ' + Ext.htmlEncode(task.get('name')));
        });
        tasksField.setValue((tasksList.length == 0) ? tasksList = '-' : tasksList.join('<br/>'));
        Ext.resumeLayouts();
    },

    showAddMessageQueue: function () {
        var me = this;
        router = me.getController('Uni.controller.history.Router');
        // view = Ext.widget('add-queue-message-form');
        view = Ext.create('Apr.view.messagequeues.AddMessageQueueForm', {
            router: router
        });
        me.getApplication().fireEvent('changecontentevent', view);
    }


});
