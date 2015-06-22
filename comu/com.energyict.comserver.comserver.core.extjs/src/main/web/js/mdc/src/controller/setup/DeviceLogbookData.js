Ext.define('Mdc.controller.setup.DeviceLogbookData', {
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
        'Mdc.store.EventsOrActions',
        'Mdc.store.LogbooksOfDevice'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLogbookData'
        },
        {
            ref: 'deviceLogBookDetailTitle',
            selector: '#deviceLogBookDetailTitle'
        }
    ],

    loadProfileModel: null,

    init: function () {
        this.control({});
    },

    showOverview: function (mRID, logbookId, tabController) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            dataStore = me.getStore('Mdc.store.LogbookOfDeviceData'),
            dataStoreProxy = dataStore.getProxy(),
            widget,
            data,
            logbooksOfDeviceStore = me.getStore('Mdc.store.LogbooksOfDevice'),
            sideFilter;

        dataStoreProxy.setUrl({
            mRID: mRID,
            logbookId: logbookId
        });

        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);

                widget = Ext.widget('tabbedDeviceLogBookView', {
                    device: record,
                    router: me.getController('Uni.controller.history.Router')
                });

                data = Ext.widget('deviceLogbookData', {
                    router: me.getController('Uni.controller.history.Router'),
                    device: record,
                    side: false,
                    eventsView: false
                });

                var func = function () {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.down('#logBook-data').add(data);
                    tabController.showTab(1);

                    Uni.util.Common.loadNecessaryStores([
                        'Mdc.store.Domains',
                        'Mdc.store.Subdomains',
                        'Mdc.store.EventsOrActions'
                    ], function () {
                        // Do nothing.
                    });

                    logbookModel.getProxy().setUrl(mRID);
                    logbookModel.load(logbookId, {
                        success: function (record) {
                            me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                            widget.down('#logBookTabPanel').setTitle(record.get('name'));
                        }
                    });
                };

                if (logbooksOfDeviceStore.getTotalCount() === 0) {
                    logbooksOfDeviceStore.getProxy().setUrl(mRID);
                    logbooksOfDeviceStore.load(function () {
                        func();
                    });
                } else {
                    func();
                }
            }
        });


    }
});