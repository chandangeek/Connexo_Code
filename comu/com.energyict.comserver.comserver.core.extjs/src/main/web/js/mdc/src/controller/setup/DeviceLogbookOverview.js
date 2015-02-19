Ext.define('Mdc.controller.setup.DeviceLogbookOverview', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.TimeUnits'
    ],

    stores: [
        'TimeUnits',
        'Mdc.store.LogbooksOfDevice'
    ],

    views: [
        'Mdc.view.setup.devicelogbooks.Overview',
        'Mdc.view.setup.devicelogbooks.TabbedDeviceLogBookView'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LogbookOfDevice'
    ],

    refs: [
        {
            ref: 'deviceLogBookDetailTitle',
            selector: '#deviceLogBookDetailTitle'
        }
    ],

    showOverview: function (mRID, logbookId, tabController) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            logbooksOfDeviceStore = me.getStore('Mdc.store.LogbooksOfDevice'),
            widget;

        deviceModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                var overview = Ext.widget('deviceLogbookOverview', {
                    router: me.getController('Uni.controller.history.Router')
                });
                widget = Ext.widget('tabbedDeviceLogBookView', {
                    device: record,
                    router: me.getController('Uni.controller.history.Router')
                });
                var func = function () {
                    widget.setLoading(true);
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.down('#logBook-specifications').add(overview);
                    tabController.showTab(0);
                    logbookModel.getProxy().setUrl(mRID);
                    logbookModel.load(logbookId, {
                        success: function (record) {
                            me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                            widget.down('#logBookTabPanel').setTitle(record.get('name'));
                            widget.down('#deviceLogbooksPreviewForm').loadRecord(record);
                            widget.setLoading(false);
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