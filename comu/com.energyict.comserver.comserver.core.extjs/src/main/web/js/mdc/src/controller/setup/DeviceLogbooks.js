/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceLogbooks', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Common'
    ],

    views: [
        'Mdc.view.setup.devicelogbooks.Setup',
        'Mdc.view.setup.devicelogbooks.EditWindow'
    ],

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.store.LogbooksOfDevice'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'deviceLogbooksSetup #deviceLogbooksPreview'
        },
        {
            ref: 'deviceLogbookEditWindow',
            selector: 'devicelogbook-edit-window'
        }
    ],

    init: function () {
        this.control({
            'deviceLogbooksSetup #deviceLogbooksGrid': {
                select: this.showPreview
            },
            '#deviceLogbooksActionMenu': {
                click: this.onDeviceLogbookAction
            },
            '#mdc-devicelogbook-edit-window-save': {
                click: this.saveLogBook
            }
        });
    },

    showView: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            model = me.getModel('Mdc.model.Device'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            widget;

        viewport.setLoading();

        me.getStore('Mdc.store.LogbooksOfDevice').getProxy().setExtraParam('deviceId', deviceId);
        model.load(deviceId, {
            success: function (record) {
                if (record.get('hasLogBooks')) {
                    widget = Ext.widget('deviceLogbooksSetup', {
                        device: record,
                        router: router,
                        toggleId: 'logbooksLink'
                    });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    me.getApplication().fireEvent('loadDevice', record);
                } else {
                    window.location.replace(router.getRoute('notfound').buildUrl());
                }
                viewport.setLoading(false);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        this.getPreview().setLogbook(record);
    },

    onDeviceLogbookAction: function (menu, item) {
        switch (item.action) {
            case 'editLogbook':
                Ext.widget('devicelogbook-edit-window', {
                    logbookRecord: menu.record
                }).show();
                break;
        }
    },

    saveLogBook: function() {
        var me = this,
            editWindow = me.getDeviceLogbookEditWindow(),
            datePicker = editWindow.down('#mdc-devicelogbook-edit-window-date-picker'),
            logbookRecordInGrid = editWindow.logbookRecord,
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            deviceId = this.getController('Uni.controller.history.Router').arguments.deviceId,
            logbookId = logbookRecordInGrid.get('id'),
            onLogbookLoaded = function(logbookRecord) {
                logbookRecordInGrid.set('lastReading', datePicker.getValue());
                logbookRecord.beginEdit();
                logbookRecord.set('lastReading', datePicker.getValue());
                if (!logbookRecord.get('lastEventType')) {
                    logbookRecord.set('lastEventType', null);
                }
                if (!logbookRecord.get('lastEventDate')) {
                    logbookRecord.set('lastEventDate', null);
                }
                logbookRecord.endEdit();
                logbookRecord.save({
                    success: onLogBookSaved
                });
                editWindow.close();
            },
            onLogBookSaved = function() {
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('devicelogbooks.acknowledge.updateSuccess', 'MDC', 'Logbook saved')
                );
                me.getPreview().setLogbook(logbookRecordInGrid);
            };

        logbookModel.getProxy().setExtraParam('deviceId', Uni.util.Common.decodeURIArguments(deviceId));
        logbookModel.load(logbookId, {
            success: onLogbookLoaded
        });
    }
});