Ext.define('Mdc.controller.setup.DeviceLogbookOverview', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.TimeUnits'
    ],

    stores: [
      'TimeUnits'
    ],

    views: [
        'Mdc.view.setup.devicelogbooks.Overview'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LogbookOfDevice'
    ],

    showOverview: function (mRID, logbookId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice');

        deviceModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                var widget = Ext.widget('deviceLogbookOverview', {
                    router: me.getController('Uni.controller.history.Router'),
                    device: record,
                    toggleId: 'events'
                });
                widget.setLoading(true);
                me.getApplication().fireEvent('changecontentevent', widget);
                logbookModel.getProxy().setUrl(mRID);
                logbookModel.load(logbookId, {
                    success: function (record) {
                        me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                        widget.down('#deviceLogbooksPreviewForm').loadRecord(record);
                        widget.down('#deviceLogbookSubMenuPanel').setParams(mRID, record);
                        widget.setLoading(false);
                    }
                });
            }
        });


    }
});