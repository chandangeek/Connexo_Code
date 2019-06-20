/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.controller.ServiceCallTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'Sct.view.Setup',
        'Sct.view.LogLevelWindow',
        'Sct.view.QueueAndPriorityWindow'
    ],
    stores: [
        'Sct.store.ServiceCallTypes',
        'Sct.store.LogLevels',
        'Sct.store.AvailableQueues'
    ],
    models: [
    ],

    refs: [
        {
            ref: 'page',
            selector: 'servicecalltypes-setup'
        },
        {
            ref: 'changeLogLevelWindow',
            selector: 'log-level-window'
        },
        {
            ref: 'queuePriorityWindow',
            selector: 'queue-priority-window'
        }
    ],

    init: function () {
        this.control({
            'servicecalltypes-setup servicecalltypes-grid': {
                select: this.showPreview
            },
            'sct-action-menu': {
                click: this.chooseAction
            },
            'log-level-window #save-log-level-button': {
                click: this.updateLogLevel
            },
            'log-level-window': {
                close: this.closeLogLevelWindow
            },
            'queue-priority-window #save-queue-priority-button': {
                click: this.saveQueuePriority
            },
            'queue-priority-window': {
                close: this.closeQueuePriority
            }
        });
    },

    showServiceCallTypes: function(){
        var me = this,
            view = Ext.widget('servicecalltypes-setup');

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('servicecalltypes-preview'),
            serviceCallTypeName = record.get('name'),
            previewForm = page.down('servicecalltypes-preview-form');

        preview.setTitle(Ext.String.htmlEncode(serviceCallTypeName));
        previewForm.updatePreview(record, preview);
        preview.down('sct-action-menu').record = record;
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'changeLogLevel':
                me.changeLogLevel(menu.record);
                break;
            case 'setQueueAndPriority':
                me.setQueueAndPriority(menu.record);
                break;
        }
    },

    changeLogLevel: function (record) {
        var me = this,
            store = Ext.getStore('Sct.store.LogLevels'),
            changeLogLevelWindow = Ext.widget('log-level-window', {
                record: record,
                store: store
            });

        me.getPage().setLoading();
        store.load(function(records, operation, success) {
            if(success) {
                changeLogLevelWindow.show();
            }
            me.getPage().setLoading(false);
        });
    },

    setQueueAndPriority: function (record) {
        var me = this,
            store = Ext.getStore('Sct.store.AvailableQueues');

        var changeLogLevelWindow = Ext.widget('queue-priority-window', {
            record: record,
            store: store
        });

        me.getPage().setLoading();
        store.load(function(records, operation, success) {
            if (success) {
                changeLogLevelWindow.show();
            }
            me.getPage().setLoading(false);
        });
    },

    updateLogLevel: function() {
        var me = this,
            window = me.getChangeLogLevelWindow(),
            record = window.record,
            combobox = window.down('#log-level-field'),
            logLevel = combobox.findRecordByDisplay(combobox.getRawValue());

        record.set('logLevel', logLevel.data);
        record.save({
            success: function (record) {
                me.getPage().down('#grd-service-call-types').getStore().load();
                window.close();
            }
        });
    },

    saveQueuePriority: function() {
        var me = this,
            window = me.getQueuePriorityWindow(),
            record = window.record;
            form = window.down('#queue-priority-form');

        form.updateRecord();
        record.save({
            success: function (record) {
                me.getStore('Sct.store.ServiceCallTypes').load();
                window.close();
            }
        });
    },

    closeLogLevelWindow: function() {
        var me = this;
        me.getPage().setLoading(false);
    },

    closeQueuePriority: function() {
        var me = this;
        me.getPage().setLoading(false);
    }
});