Ext.define('Mdc.controller.setup.DeviceLogbooks', {
    extend: 'Ext.app.Controller',

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

    showView: function (mRID) {
        var me = this,
            model = me.getModel('Mdc.model.Device'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            widget;

        viewport.setLoading();

        me.getStore('Mdc.store.LogbooksOfDevice').getProxy().setUrl(mRID);
        model.load(mRID, {
            success: function (record) {
                widget = Ext.widget('deviceLogbooksSetup', {
                    device: record,
                    router: me.getController('Uni.controller.history.Router'),
                    toggleId: 'logbooksLink'
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDevice', record);
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
            deviceMRID = this.getController('Uni.controller.history.Router').arguments.mRID,
            logbookId = logbookRecordInGrid.get('id'),
            onLogbookLoaded = function(logbookRecord) {
                debugger;
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

        logbookModel.getProxy().setUrl(deviceMRID);
        logbookModel.load(logbookId, {
            success: onLogbookLoaded
        });
    }
});