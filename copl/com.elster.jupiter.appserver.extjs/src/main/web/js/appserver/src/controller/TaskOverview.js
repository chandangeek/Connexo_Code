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
        'Apr.store.Queues'
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

    setQueue: function (record) {

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            itemId: 'setQueueConfirmationWindow',
            confirmText: Uni.I18n.translate('general.save', 'APR', 'Save'),
            closeAction: 'destroy',
            green: true,
            confirmation: Ext.bind(this.onCheck, this, [getConfirmationWindow])
        });

        confirmationWindow.insert(1, {
            xtype: 'set-queue',
            itemId: 'set-queue-item',
            padding: '-10 0 0 -20'
        });
        // confirmationWindow.insert(1, {
        //     itemId: 'snooze-now-window-errors',
        //     xtype: 'label',
        //     margin: '0 0 10 50',
        //     hidden: true
        // });
        confirmationWindow.show({
            // title: Uni.I18n.translate('general.setqueue', 'APR', "Set queue '{0}'?", record.getData('name'), false)
            title: Uni.I18n.translate('general.setqueue', 'APR', "Set queue '{0}'?", record.data.name, false)
        });

        function getConfirmationWindow() {
            return confirmationWindow;
        }
    }
});
