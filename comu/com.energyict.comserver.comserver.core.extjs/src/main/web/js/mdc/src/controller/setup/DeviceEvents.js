Ext.define('Mdc.controller.setup.DeviceEvents', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Common'
    ],

    views: [
        'Mdc.view.setup.deviceevents.Data'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LogbookOfDevice',
        'Mdc.model.LogbookOfDeviceDataFilter'
    ],

    stores: [
        'Mdc.store.LogbookOfDeviceData',
        'Mdc.store.Domains',
        'Mdc.store.Subdomains',
        'Mdc.store.EventsOrActions'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLogbookData'
        }
    ],

    loadProfileModel: null,

    init: function () {
        this.control({
            'deviceLogbookData #deviceLogbookDataGrid': {
                select: this.showPreview
            }
        });
    },


    showOverview: function (deviceId, logbookId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            dataStore = me.getStore('Mdc.store.LogbookOfDeviceData'),
            dataStoreProxy = dataStore.getProxy(),
            widget,
            toggleId = 'events',
            title = Uni.I18n.translate('general.events', 'MDC', 'Events');

        dataStoreProxy.setUrl({deviceId: deviceId, logbookId: logbookId});
        if (Ext.isDefined(logbookId)) {
            title = Uni.I18n.translate('devicelogbooks.event.header', 'MDC', 'Logbook events');
            toggleId = 'logbooksLink';
            logbookModel.getProxy().setExtraParam('deviceId', deviceId);
            logbookModel.load(logbookId, {
                success: function (record) {
                    me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                }
            });
        }

        me.getModel('Mdc.model.Device').load(deviceId, {
            success: function (record) {
                if (record.get('hasLogBooks')) {
                    me.getApplication().fireEvent('loadDevice', record);
                    widget = Ext.widget('deviceLogbookData', {
                        router: router,
                        device: record,
                        title: title,
                        toggleId: toggleId,
                        eventsView: true
                    });
                    me.getApplication().fireEvent('changecontentevent', widget);

                    Uni.util.Common.loadNecessaryStores([
                        'Mdc.store.Domains',
                        'Mdc.store.Subdomains',
                        'Mdc.store.EventsOrActions'
                    ], function () {
                        // Do nothing as callback.
                    });
                } else {
                    window.location.replace(router.getRoute('notfound').buildUrl());
                }
            }
        });
    },

    showPreview: function (selectionModel, record) {
        this.getPage().down('#deviceLogbookDataPreview').loadData(record);
    }
});
