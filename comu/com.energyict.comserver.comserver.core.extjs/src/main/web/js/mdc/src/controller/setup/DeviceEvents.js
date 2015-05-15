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


    showOverview: function (mRID, logbookId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            dataStore = me.getStore('Mdc.store.LogbookOfDeviceData'),
            dataStoreProxy = dataStore.getProxy(),
            widget,
            toggleId = 'events',
            title = Uni.I18n.translate('deviceevents.header', 'MDC', 'Events');

        dataStoreProxy.setUrl({mRID: mRID, logbookId: logbookId});
        if (Ext.isDefined(logbookId)) {
            title = Uni.I18n.translate('devicelogbooks.event.header', 'MDC', 'Logbook events');
            toggleId = 'logbooksLink';
            logbookModel.getProxy().setUrl(mRID);
            logbookModel.load(logbookId, {
                success: function (record) {
                    me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                }
            });
        }

        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                widget = Ext.widget('deviceLogbookData', {
                    router: me.getController('Uni.controller.history.Router'),
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
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPage().down('#deviceLogbookDataPreview');
        preview.setTitle(Uni.DateTime.formatDateTimeLong(record.get('eventDate')));
        preview.down('#deviceLogbookDataPreviewForm').loadRecord(record);
    }
});
